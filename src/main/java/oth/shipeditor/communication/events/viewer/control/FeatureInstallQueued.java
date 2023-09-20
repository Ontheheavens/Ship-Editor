package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

import java.awt.geom.Point2D;

/**
 * Position is also expected to be adjusted and corrected by grid if respective option is enabled.
 * @author Ontheheavens
 * @since 19.09.2023
 */
public record FeatureInstallQueued(Point2D worldPosition) implements ViewerEvent {

}
