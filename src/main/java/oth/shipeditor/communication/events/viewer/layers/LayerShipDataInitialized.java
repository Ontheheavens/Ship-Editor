package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.LayerPainter;

/**
 * @author Ontheheavens
 * @since 03.06.2023
 */
public record LayerShipDataInitialized(LayerPainter source, int ordering) implements LayerEvent {

}
