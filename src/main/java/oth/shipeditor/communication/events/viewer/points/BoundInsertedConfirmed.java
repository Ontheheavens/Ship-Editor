package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.BoundPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record BoundInsertedConfirmed(BoundPoint toInsert, int precedingIndex) implements PointEvent {

}
