package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 14.06.2023
 */
public record RadiusDragQueued(Point2D location) implements PointEvent{

}
