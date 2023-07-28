package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

/**
 * @author Ontheheavens
 * @since 03.06.2023
 */
public record LayerShipDataInitialized(ShipPainter source) implements LayerEvent {

}
