package oth.shipeditor.communication.events.viewer.layers.ships;

import oth.shipeditor.communication.events.viewer.layers.LayerEvent;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
public record ShipDataCreated(ShipLayer layer) implements LayerEvent {

}
