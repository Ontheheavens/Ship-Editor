package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.WorldPoint;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 17.06.2023
 */
public record AnchorOffsetConfirmed(WorldPoint point, Point2D difference) implements PointEvent{

}
