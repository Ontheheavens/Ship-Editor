package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.communication.events.viewer.ViewerEvent;
import oth.shipeditor.components.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record PointSelectQueued<T extends WorldPoint>(T point) implements ViewerEvent {
}
