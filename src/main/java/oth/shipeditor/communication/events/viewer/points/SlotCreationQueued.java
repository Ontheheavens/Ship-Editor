package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public record SlotCreationQueued(Point2D position) implements PointEvent{

}
