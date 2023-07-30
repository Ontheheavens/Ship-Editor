package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ViewerLayer;

/**
 * @author Ontheheavens
 * @since 03.06.2023
 */
public record LayerWasSelected(ViewerLayer old, ViewerLayer selected) implements LayerEvent {
}
