package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ShipLayer;

/**
 * Note: layer instance argument is always assumed by recipients to be active layer.
 * Passing any other than active layer as argument will likely lead to subtle bugs.
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record ActiveLayerUpdated(ShipLayer updated, boolean spriteChanged) implements LayerEvent {

}
