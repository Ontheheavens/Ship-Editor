package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record PointDragQueued(AffineTransform screenToWorld, Point2D target) implements PointEvent {

}
