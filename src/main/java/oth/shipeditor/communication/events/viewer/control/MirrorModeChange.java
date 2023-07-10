package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 09.07.2023
 */
public record MirrorModeChange(boolean enabled) implements ViewerEvent {

}
