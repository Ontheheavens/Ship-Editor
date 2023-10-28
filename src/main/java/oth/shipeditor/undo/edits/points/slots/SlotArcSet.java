package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 09.08.2023
 */
public class SlotArcSet extends AbstractEdit {

    private final SlotData slotPoint;

    private final double oldArc;

    private final double updatedArc;

    public SlotArcSet(SlotData point, double old, double updated) {
        this.slotPoint = point;
        this.oldArc = old;
        this.updatedArc = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        slotPoint.setArc(oldArc);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public void redo() {
        slotPoint.setArc(updatedArc);
        redoSubEdits();
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Slot Arc";
    }

}
