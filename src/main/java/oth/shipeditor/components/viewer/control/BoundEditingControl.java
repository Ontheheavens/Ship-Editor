package oth.shipeditor.components.viewer.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.Predicates;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
class BoundEditingControl {

    @Getter @Setter
    private Point2D adjustedCursor;

    // TODO: unify hotkeys.

    private final Predicate<MouseEvent> appendPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.shiftDown()
    );

    private final Predicate<MouseEvent> insertPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.controlDown()
    );

    void tryBoundCreation(MouseEvent event, AffineTransform screenToWorld) {
        boolean appendFalse = !this.appendPointPredicate.test(event);
        boolean insertFalse = !this.insertPointPredicate.test(event);
        if (appendFalse && insertFalse) return;
        Point2D screenPoint = this.adjustedCursor;
        Point2D rounded = Utility.correctAdjustedCursor(screenPoint, screenToWorld);
        EventBus.publish(new BoundCreationQueued(rounded, appendFalse));
    }

}
