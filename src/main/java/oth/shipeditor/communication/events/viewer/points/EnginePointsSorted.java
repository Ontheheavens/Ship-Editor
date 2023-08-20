package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.engine.EnginePoint;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public record EnginePointsSorted(List<EnginePoint> rearranged) implements PointEvent {

}
