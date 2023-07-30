package oth.shipeditor.communication.events.viewer.layers.ships;

import oth.shipeditor.communication.events.viewer.layers.LayerEvent;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record ShipLayerCreated(ShipLayer newLayer) implements LayerEvent {

}
