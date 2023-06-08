package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;
import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 05.06.2023
 */
public record LayerAnchorDragged(AffineTransform screenToWorld, LayerPainter selected,
                                 Point2D difference) implements ViewerEvent {
}
