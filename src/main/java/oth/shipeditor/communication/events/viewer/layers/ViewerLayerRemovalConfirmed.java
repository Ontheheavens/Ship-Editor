package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ViewerLayer;

/**
 * @author Ontheheavens
 * @since 04.06.2023
 */
public record ViewerLayerRemovalConfirmed(ViewerLayer removed) implements LayerEvent {

}
