package oth.shipeditor.communication.events.viewer.points;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
@Log4j2
public record PointSelectedConfirmed(WorldPoint point) implements PointEvent {

}
