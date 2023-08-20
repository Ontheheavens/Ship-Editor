package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public record EngineSizeChangeQueued(Point2D worldTarget) implements PointEvent {

}
