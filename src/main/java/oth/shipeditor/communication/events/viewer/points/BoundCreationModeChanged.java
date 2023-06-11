package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.InteractionMode;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record BoundCreationModeChanged(InteractionMode newMode) implements PointEvent {

}
