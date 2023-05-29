package oth.shipeditor.components.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.Predicates;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.BoundCreationQueued;
import oth.shipeditor.utility.Utility;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
public class BoundEditingControl {

    private final ShipViewerControls parentControls;

    private final Predicate<MouseEvent> appendPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.shiftDown()
    );

    private final Predicate<MouseEvent> insertPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.altDown()
    );

    protected BoundEditingControl(ShipViewerControls parent) {
        this.parentControls = parent;
    }

    protected void tryBoundCreation(MouseEvent event, AffineTransform screenToWorld) {
        boolean append = appendPointPredicate.test(event);
        boolean insert = insertPointPredicate.test(event);
        if (!append && !insert) return;
        Point2D screenPoint = parentControls.getAdjustedCursor();
        Point2D rounded = Utility.correctAdjustedCursor(screenPoint, screenToWorld);
        EventBus.publish(new BoundCreationQueued(rounded, !append));
    }

}
