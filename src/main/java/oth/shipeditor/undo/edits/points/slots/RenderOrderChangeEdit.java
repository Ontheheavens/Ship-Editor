package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 24.10.2023
 */
public class RenderOrderChangeEdit extends AbstractEdit {

    private final SlotData slotPoint;

    private final int oldOrder;

    private final int updatedOrder;

    public RenderOrderChangeEdit(SlotData point, int old, int updated) {
        this.slotPoint = point;
        this.oldOrder = old;
        this.updatedOrder = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        slotPoint.setRenderOrderMod(oldOrder);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public void redo() {
        slotPoint.setRenderOrderMod(updatedOrder);
        redoSubEdits();
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Render Order";
    }

}
