package oth.shipeditor.undo.edits.points;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.painters.points.BoundPointsPainter;
import oth.shipeditor.undo.AbstractEdit;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
public class BoundsSortEdit extends AbstractEdit {

    private final BoundPointsPainter pointPainter;

    private final List<BoundPoint> oldList;

    private final List<BoundPoint> newList;

    public BoundsSortEdit(BoundPointsPainter painter, List<BoundPoint> old, List<BoundPoint> changed) {
        this.pointPainter = painter;
        this.oldList = old;
        this.newList = changed;
    }

    @Override
    public void undo() {
        pointPainter.setBoundPoints(oldList);
        Events.repaintView();
    }

    @Override
    public void redo() {
        pointPainter.setBoundPoints(newList);
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Sort Bounds";
    }

}
