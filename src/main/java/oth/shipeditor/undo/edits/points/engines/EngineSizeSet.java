package oth.shipeditor.undo.edits.points.engines;

import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineSizeSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final Size2D oldSize;

    private final Size2D updatedSize;

    public EngineSizeSet(EnginePoint point, Size2D old, Size2D updated) {
        this.enginePoint = point;
        this.oldSize = old;
        this.updatedSize = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        enginePoint.setSize(oldSize);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public void redo() {
        enginePoint.setSize(updatedSize);
        redoSubEdits();
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Engine Size";
    }

}
