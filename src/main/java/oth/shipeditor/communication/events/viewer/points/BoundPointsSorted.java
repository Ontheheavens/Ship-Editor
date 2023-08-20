package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.BoundPoint;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
public record BoundPointsSorted(List<BoundPoint> rearranged) implements PointEvent {

}
