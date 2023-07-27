package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public record LayerRotationQueued(LayerPainter layer, Point2D worldTarget) implements LayerEvent{

}
