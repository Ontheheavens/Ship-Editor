package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record BoundCreationQueued(Point2D position, boolean toInsert) implements PointEvent {

}
