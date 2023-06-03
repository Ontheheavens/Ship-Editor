package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record PointAddConfirmed(WorldPoint point) implements PointEvent {

}
