package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ShipLayer;

/**
 * @author Ontheheavens
 * @since 03.06.2023
 */
public record LayerWasSelected(ShipLayer old, ShipLayer selected) implements LayerEvent {
}
