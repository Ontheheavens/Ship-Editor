package oth.shipeditor.communication.events.viewer.layers;

import de.javagl.viewer.Painter;

/**
 * @author Ontheheavens
 * @since 03.06.2023
 */
public record PainterAdditionQueued(Painter toAdd, int ordering) implements LayerEvent {

}
