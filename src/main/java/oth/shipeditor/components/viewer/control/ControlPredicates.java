package oth.shipeditor.components.viewer.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.Predicates;

import java.awt.event.MouseEvent;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
final class ControlPredicates {

    static final Predicate<MouseEvent> translatePredicate = Predicates.and(
            InputEventPredicates.buttonDown(3),
            InputEventPredicates.noModifiers()
    );

    static final Predicate<MouseEvent> layerMovePredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.shiftDown()
    );

    static final Predicate<MouseEvent> selectPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.noModifiers()
    );

    static final Predicate<MouseEvent> removePointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(3),
            InputEventPredicates.shiftDown()
    );

    static final Predicate<MouseEvent> rotatePredicate = InputEventPredicates.controlDown();

    private ControlPredicates() {
    }

}
