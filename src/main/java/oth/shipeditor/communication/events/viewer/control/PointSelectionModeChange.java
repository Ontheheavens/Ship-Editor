package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;
import oth.shipeditor.components.viewer.control.PointSelectionMode;

/**
 * @author Ontheheavens
 * @since 09.07.2023
 */
public record PointSelectionModeChange(PointSelectionMode newMode) implements ViewerEvent {

}
