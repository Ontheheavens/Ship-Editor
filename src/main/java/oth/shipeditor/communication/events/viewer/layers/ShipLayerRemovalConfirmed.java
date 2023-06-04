package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ShipLayer;

/**
 * @author Ontheheavens
 * @since 04.06.2023
 */
public record ShipLayerRemovalConfirmed(ShipLayer removed) implements LayerEvent {

}
