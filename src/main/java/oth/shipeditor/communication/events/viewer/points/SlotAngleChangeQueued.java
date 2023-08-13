package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public record SlotAngleChangeQueued(Point2D worldTarget) implements PointEvent {

}
