package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.representation.ShipLayer;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public record ShipLayerLoadConfirmed(ShipLayer layer) implements LayerEvent {

}
