package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public record RotationRoundingToggled(boolean toggled) implements ViewerEvent {

}
