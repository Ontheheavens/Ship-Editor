package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 13.07.2023
 */
public record ViewerTransformRotated(double degrees) implements ViewerEvent {

}
