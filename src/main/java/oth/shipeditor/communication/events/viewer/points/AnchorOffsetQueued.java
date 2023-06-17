package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 17.06.2023
 */
public record AnchorOffsetQueued(LayerPainter layer, Point2D difference) implements PointEvent {
}
