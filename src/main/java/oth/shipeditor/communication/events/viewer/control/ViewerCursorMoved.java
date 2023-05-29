package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record ViewerCursorMoved(Point2D rawCursor,
                                Point2D adjusted,
                                Point2D adjustedAndCorrected)
        implements ViewerEvent {

}
