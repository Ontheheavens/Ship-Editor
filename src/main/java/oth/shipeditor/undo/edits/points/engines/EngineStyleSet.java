package oth.shipeditor.undo.edits.points.engines;

import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

/**
 * @author Ontheheavens
 * @since 22.08.2023
 */
public class EngineStyleSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final EngineStyle oldStyle;

    private final EngineStyle updatedStyle;

    public EngineStyleSet(EnginePoint point, EngineStyle old, EngineStyle updated) {
        this.enginePoint = point;
        this.oldStyle = old;
        this.updatedStyle = updated;
    }

    @Override
    public void undo() {
        enginePoint.setStyle(oldStyle);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public void redo() {
        enginePoint.setStyle(updatedStyle);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    @Override
    public String getName() {
        return "Change Engine Style";
    }

}
