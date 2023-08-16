package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
public record PointCreationQueued(Point2D position) implements PointEvent {

}
