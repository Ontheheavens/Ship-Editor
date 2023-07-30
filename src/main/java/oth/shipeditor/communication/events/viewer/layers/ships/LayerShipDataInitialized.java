package oth.shipeditor.communication.events.viewer.layers.ships;

import oth.shipeditor.communication.events.viewer.layers.LayerEvent;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

/**
 * @author Ontheheavens
 * @since 03.06.2023
 */
public record LayerShipDataInitialized(ShipPainter source) implements LayerEvent {

}
