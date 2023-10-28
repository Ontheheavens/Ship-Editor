package oth.shipeditor.undo.edits;

import lombok.Getter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.points.ship.CenterPointPainter;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.undo.Edit;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.geom.Point2D;
import java.util.Deque;

/**
 * @author Ontheheavens
 * @since 09.10.2023
 */
public class ModuleAnchorEdit extends AbstractEdit implements LayerEdit {

    @Getter
    private CenterPointPainter centersPainter;

    private final Point2D oldAnchor;

    private final Point2D updatedAnchor;

    public ModuleAnchorEdit(CenterPointPainter painter, Point2D old, Point2D updated) {
        this.centersPainter = painter;
        this.oldAnchor = old;
        this.updatedAnchor = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        centersPainter.setModuleAnchorOffset(oldAnchor);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueCenterPanelsRepaint();
        repainter.queueModuleControlRepaint();
    }

    @Override
    public void redo() {
        centersPainter.setModuleAnchorOffset(updatedAnchor);
        redoSubEdits();
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueCenterPanelsRepaint();
        repainter.queueModuleControlRepaint();
    }

    @Override
    public LayerPainter getLayerPainter() {
        return centersPainter.getParentLayer();
    }

    @Override
    public String getName() {
        return "Module Anchor Change";
    }

    @Override
    public void cleanupReferences() {
        Deque<Edit> subEdits = this.getSubEdits();
        subEdits.forEach(edit -> {
            if (edit instanceof AnchorOffsetEdit checked) {
                checked.cleanupReferences();
            }
        });
        this.centersPainter = null;
    }

}
