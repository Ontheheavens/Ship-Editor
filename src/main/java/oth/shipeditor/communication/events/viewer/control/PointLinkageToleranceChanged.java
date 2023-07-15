package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 15.07.2023
 */
public record PointLinkageToleranceChanged(int changed) implements ViewerEvent {

}
