package oth.shipeditor.undo;

import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.control.ViewerMouseReleased;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.undo.edits.PointDragEdit;

import java.awt.geom.Point2D;

/**
 * Convenience class meant to free component classes from burden of also implementing all the edit dispatch methods.
 * @author Ontheheavens
 * @since 16.06.2023
 */
public final class EditDispatcher {

    private EditDispatcher() {
    }

    public static void postPointDragEdit(WorldPoint selected, Point2D changedPosition) {
        Point2D position = selected.getPosition();
        Point2D wrappedOld = new Point2D.Double(position.getX(), position.getY());
        Point2D wrappedNew = new Point2D.Double(changedPosition.getX(), changedPosition.getY());
        PointDragEdit edit = new PointDragEdit(selected, wrappedOld, wrappedNew);
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (previousEdit instanceof PointDragEdit checked && !checked.isFinished()) {
            edit.setFinished(true);
            checked.add(edit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !edit.isFinished()) {
                        edit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(edit);
        }
        selected.setPosition(changedPosition);
        Events.repaintView();
    }

}