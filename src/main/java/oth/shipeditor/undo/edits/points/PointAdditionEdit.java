package oth.shipeditor.undo.edits.points;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.LaunchBayPainter;
import oth.shipeditor.components.viewer.painters.points.MirrorablePointPainter;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.undo.Edit;

import java.util.Deque;

/**
 * Also handles point insertion cases.
 * @author Ontheheavens
 * @since 17.06.2023
 */
public class PointAdditionEdit extends AbstractEdit implements PointEdit {

    private AbstractPointPainter pointPainter;
    private BaseWorldPoint point;
    private final int insertionIndex;

    public PointAdditionEdit(AbstractPointPainter painter, BaseWorldPoint toAdd) {
        this(painter, toAdd, -1);
    }

    public PointAdditionEdit(AbstractPointPainter painter, BaseWorldPoint toAdd, int index) {
        this.pointPainter = painter;
        this.point = toAdd;
        this.insertionIndex = index;
    }

    @Override
    public WorldPoint getPoint() {
        return point;
    }

    @Override
    public void undo() {
        pointPainter.removePoint(point);
        Events.repaintShipView();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public void redo() {
        if (insertionIndex == -1 || pointPainter instanceof LaunchBayPainter) {
            pointPainter.addPoint(point);
        } else if (pointPainter instanceof MirrorablePointPainter checked) {
            checked.insertPoint(point, insertionIndex);
        }
        Events.repaintShipView();
    }

    @Override
    public String getName() {
        String name = "Add Point";
        if (insertionIndex != -1) {
            name = "Insert Point";
        }
        return name;
    }

    @Override
    public LayerPainter getLayerPainter() {
        return point.getParent();
    }

    @Override
    public void cleanupReferences() {
        Deque<Edit> subEdits = this.getSubEdits();
        subEdits.forEach(edit -> {
            if (edit instanceof PointAdditionEdit checked) {
                checked.cleanupReferences();
            }
        });
        this.pointPainter = null;
        this.point = null;
    }

}
