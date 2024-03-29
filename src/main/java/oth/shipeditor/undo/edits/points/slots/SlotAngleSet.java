package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class SlotAngleSet extends AbstractEdit {

    private final SlotData slotPoint;

    private final double oldAngle;

    private final double updatedAngle;

    public SlotAngleSet(SlotData point, double old, double updated) {
        this.slotPoint = point;
        this.oldAngle = old;
        this.updatedAngle = updated;
        this.setFinished(false);
    }


    @Override
    public void undo() {
        undoSubEdits();
        slotPoint.setAngle(oldAngle);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueSlotsPanelRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public void redo() {
        slotPoint.setAngle(updatedAngle);
        redoSubEdits();
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueSlotsPanelRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Slot Angle";
    }

}
