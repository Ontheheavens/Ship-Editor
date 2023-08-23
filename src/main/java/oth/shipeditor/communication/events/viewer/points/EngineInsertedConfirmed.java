package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.engine.EnginePoint;

/**
 * @author Ontheheavens
 * @since 23.08.2023
 */
public record EngineInsertedConfirmed(EnginePoint toInsert, int precedingIndex) implements PointEvent {

}
