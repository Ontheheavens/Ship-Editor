package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.PointsDisplay;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record BoundCreationModeChanged(PointsDisplay.InteractionMode newMode) implements PointEvent {

}
