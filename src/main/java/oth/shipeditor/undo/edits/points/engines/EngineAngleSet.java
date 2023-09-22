package oth.shipeditor.undo.edits.points.engines;

import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineAngleSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final double oldAngle;

    private final double updatedAngle;

    public EngineAngleSet(EnginePoint point, double old, double updated) {
        this.enginePoint = point;
        this.oldAngle = old;
        this.updatedAngle = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        enginePoint.setAngle(oldAngle);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public void redo() {
        enginePoint.setAngle(updatedAngle);
        redoSubEdits();
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Engine Angle";
    }

}
