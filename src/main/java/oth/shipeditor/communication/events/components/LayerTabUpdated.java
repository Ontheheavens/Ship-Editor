package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.viewer.layers.ViewerLayer;

/**
 * @author Ontheheavens
 * @since 12.11.2023
 */
public record LayerTabUpdated(ViewerLayer layer) implements ComponentEvent {

}
