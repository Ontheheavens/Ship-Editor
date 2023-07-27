package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public record ViewerZoomSet(double level) implements ViewerEvent {

}
