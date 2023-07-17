package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.menubar.FileUtilities;

/**
 * Convenience class for static access to active layer.
 * @author Ontheheavens
 * @since 09.07.2023
 */
public final class StaticLayerController {

    @Getter
    private static ShipLayer active;

    private StaticLayerController() {
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                active = checked.selected();
                FileUtilities.updateActionStates(active);
            }
            if (event instanceof ActiveLayerUpdated checked) {
                active = checked.updated();
                FileUtilities.updateActionStates(active);
            }
        });
    }

}
