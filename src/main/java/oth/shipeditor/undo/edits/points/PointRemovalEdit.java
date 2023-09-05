package oth.shipeditor.undo.edits.points;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.LaunchBayPainter;
import oth.shipeditor.components.viewer.painters.points.MirrorablePointPainter;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.undo.Edit;

import java.util.Deque;

/**
 * @author Ontheheavens
 * @since 17.06.2023
 */
@Log4j2
@AllArgsConstructor
public class PointRemovalEdit extends AbstractEdit implements PointEdit {
    private AbstractPointPainter painter;
    private BaseWorldPoint removed;
    private final int indexOfRemoved;

    @Override
    public void undo() {
        if (painter instanceof MirrorablePointPainter checked && !(painter instanceof LaunchBayPainter)) {
            checked.insertPoint(removed, indexOfRemoved);
        } else {
            painter.addPoint(removed);
        }
        Events.repaintView();
    }

    @Override
    public WorldPoint getPoint() {
        return removed;
    }

    @Override
    public LayerPainter getLayerPainter() {
        return removed.getParentLayer();
    }

    @Override
    public void redo() {
        painter.removePoint(removed);
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Remove Point";
    }

    @Override
    public void cleanupReferences() {
        Deque<Edit> subEdits = this.getSubEdits();
        subEdits.forEach(edit -> {
            if (edit instanceof PointRemovalEdit checked) {
                checked.cleanupReferences();
            }
        });
        this.painter = null;
        this.removed = null;
    }

}
