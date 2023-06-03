package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ShipLayer;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record ShipLayerCreated(ShipLayer newLayer) implements LayerEvent {

}
