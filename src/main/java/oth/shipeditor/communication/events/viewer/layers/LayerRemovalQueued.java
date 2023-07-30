package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ViewerLayer;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
public record LayerRemovalQueued(ViewerLayer layer) implements LayerEvent {

}
