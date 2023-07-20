package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
public record PointCreationQueued(BaseWorldPoint point) implements PointEvent {

}
