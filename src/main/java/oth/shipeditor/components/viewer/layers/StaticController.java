package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.menubar.FileUtilities;

/**
 * Convenience class for static access to active layer and whatever other global features need to be accessed.
 * @author Ontheheavens
 * @since 09.07.2023
 */
public final class StaticController {

    @Getter @Setter
    private static ShipLayer activeLayer;

    @Getter @Setter
    private static double rotationRadians;

    @Getter @Setter
    private static double zoomLevel = 1;

    private StaticController() {
    }

    public static void updateRotationRadians(double input) {
        rotationRadians += input;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                activeLayer = checked.selected();
                FileUtilities.updateActionStates(activeLayer);
            }
            if (event instanceof ActiveLayerUpdated checked) {
                activeLayer = checked.updated();
                FileUtilities.updateActionStates(activeLayer);
            }
        });
    }

}
