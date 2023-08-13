package oth.shipeditor.communication.events.viewer.points;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 09.08.2023
 */
public record SlotArcChangeQueued(Point2D worldTarget) implements PointEvent {

}
