package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.representation.ShipLayer;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record ShipLayerUpdated(ShipLayer updated) implements BusEvent {

}
