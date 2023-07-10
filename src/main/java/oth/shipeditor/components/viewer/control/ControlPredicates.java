package oth.shipeditor.components.viewer.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.Predicates;
import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.MirrorModeChange;
import oth.shipeditor.communication.events.viewer.control.PointSelectionModeChange;
import oth.shipeditor.communication.events.viewer.layers.LayerShipDataInitialized;

import java.awt.event.MouseEvent;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public final class ControlPredicates {

    @Getter
    private static PointSelectionMode selectionMode = PointSelectionMode.CLOSEST;

    @Getter
    private static boolean mirrorModeEnabled = true;

    public static void initSelectionModeListening() {
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectionModeChange checked) {
                selectionMode = checked.newMode();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof MirrorModeChange checked) {
                mirrorModeEnabled = checked.enabled();
            }
        });
    }

    static final Predicate<MouseEvent> translatePredicate = Predicates.and(
            InputEventPredicates.buttonDown(2),
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

    static final Predicate<MouseEvent> rotatePredicate = InputEventPredicates.controlDown();

    private ControlPredicates() {
    }

}
