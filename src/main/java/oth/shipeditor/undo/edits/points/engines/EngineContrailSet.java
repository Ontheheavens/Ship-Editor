package oth.shipeditor.undo.edits.points.engines;

import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 22.08.2023
 */
public class EngineContrailSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final int oldContrail;

    private final int updatedContrail;

    public EngineContrailSet(EnginePoint point, int old, int updated) {
        this.enginePoint = point;
        this.oldContrail = old;
        this.updatedContrail = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        enginePoint.setContrailSize(oldContrail);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public void redo() {
        enginePoint.setContrailSize(updatedContrail);
        redoSubEdits();
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Engine Contrail Size";
    }

}
