package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.communication.events.viewer.ViewerEvent;
import oth.shipeditor.components.PointsDisplay;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record BoundCreationModeChanged(PointsDisplay.InteractionMode newMode) implements ViewerEvent {

}
