package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public record PainterOpacityChangeQueued(Class<? extends AbstractPointPainter> painterClass,
                                         float change) implements LayerEvent {

}
