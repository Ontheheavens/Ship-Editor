package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.painters.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.BoundPointsPainter;
import oth.shipeditor.undo.AbstractEdit;

/**
 * Also handles point insertion cases.
 * @author Ontheheavens
 * @since 17.06.2023
 */
public class PointAdditionEdit extends AbstractEdit implements PointEdit {

    private final AbstractPointPainter pointPainter;
    private final BaseWorldPoint point;
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
        Events.repaintView();
    }

    @Override
    public void redo() {
        if (insertionIndex == -1) {
            pointPainter.addPoint(point);
        } else if (pointPainter instanceof BoundPointsPainter checked) {
            checked.insertPoint((BoundPoint) point, insertionIndex);
        }
        Events.repaintView();
    }

    @Override
    public String getName() {
        String name = "Add Point";
        if (insertionIndex != -1) {
            name = "Insert Point";
        }
        return name;
    }

}
