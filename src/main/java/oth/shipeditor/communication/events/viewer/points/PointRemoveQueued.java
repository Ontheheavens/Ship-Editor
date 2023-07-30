package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.BaseWorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record PointRemoveQueued(BaseWorldPoint point, boolean fromList) implements PointEvent {
}
