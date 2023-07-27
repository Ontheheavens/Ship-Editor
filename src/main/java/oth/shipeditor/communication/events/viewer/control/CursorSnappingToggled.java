package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
public record CursorSnappingToggled(boolean toggled) implements ViewerEvent {

}
