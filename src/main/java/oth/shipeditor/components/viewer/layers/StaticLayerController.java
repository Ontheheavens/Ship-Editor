package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;

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

    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                active = checked.selected();
            }
        });
    }

}
