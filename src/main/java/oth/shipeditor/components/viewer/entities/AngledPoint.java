package oth.shipeditor.components.viewer.entities;

import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public abstract class AngledPoint extends BaseWorldPoint {

    protected AngledPoint(Point2D pointPosition, LayerPainter parentPainter) {
        super(pointPosition, parentPainter);
    }

    public abstract void setAngle(double degrees);

    public abstract double getAngle();

    public abstract void changeSlotAngle(double degrees);

}
