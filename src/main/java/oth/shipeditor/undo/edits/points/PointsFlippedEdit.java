package oth.shipeditor.undo.edits.points;

import lombok.AllArgsConstructor;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.undo.edits.LayerEdit;
import oth.shipeditor.utility.Utility;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 17.11.2023
 */
@AllArgsConstructor
public class PointsFlippedEdit extends AbstractEdit implements LayerEdit {

    private List<BaseWorldPoint> points;

    private BaseWorldPoint anchor;

    @Override
    public void undo() {
        for (BaseWorldPoint point : points) {
            Utility.flipPointHorizontally(point, anchor);
        }
    }

    @Override
    public void redo() {
        for (BaseWorldPoint point : points) {
            Utility.flipPointHorizontally(point, anchor);
        }
    }

    @Override
    public String getName() {
        return "Flip Ship Points";
    }

    @Override
    public LayerPainter getLayerPainter() {
        return anchor.getParent();
    }

    @Override
    public void cleanupReferences() {
        points = null;
        anchor = null;
    }

}
