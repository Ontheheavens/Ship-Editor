package oth.shipeditor.undo.edits.points;

import lombok.AllArgsConstructor;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 24.09.2023
 */
@AllArgsConstructor
public class LaunchPortsSortEdit extends AbstractEdit {

    private final LaunchPortPoint portPoint;

    private final LaunchBay targetBay;

    private final LaunchBay oldParentBay;

    private final int targetIndex;

    private final int oldIndex;

    @Override
    public void undo() {
        this.transferPort(targetBay, oldParentBay, portPoint, oldIndex);
    }

    @Override
    public void redo() {
        this.transferPort(oldParentBay, targetBay, portPoint, targetIndex);
    }

    public void transferPort(LaunchBay supplier, LaunchBay recipient, LaunchPortPoint port, int index) {
        var oldBayPoints = supplier.getPortPoints();
        oldBayPoints.remove(portPoint);

        portPoint.setParentBay(recipient);
        var portPoints = recipient.getPortPoints();
        portPoints.add(index, portPoint);

        if (StaticController.getEditorMode() == EditorInstrument.LAUNCH_BAYS) {
            // This is not optimal from performance standpoint, since the reselection triggers a very broad response;
            // Can be refactored later to only target specific bays tree refresh.
            StaticController.reselectCurrentLayer();
            var repainter = StaticController.getRepainter();
            repainter.queueViewerRepaint();
            repainter.queueBaysPanelRepaint();
        }
    }

    @Override
    public String getName() {
        return "Sort Launch Ports";
    }

}
