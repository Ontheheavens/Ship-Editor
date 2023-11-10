package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public record PainterVisibilityChanged(Class<? extends AbstractPointPainter> painterClass,
                                       PainterVisibility changed) implements LayerEvent {

}
