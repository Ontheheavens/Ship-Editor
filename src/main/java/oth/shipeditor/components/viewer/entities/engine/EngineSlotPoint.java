package oth.shipeditor.components.viewer.entities.engine;

import lombok.Setter;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public class EngineSlotPoint extends AngledPoint {

    @Setter
    private double angle;

    protected EngineSlotPoint(Point2D pointPosition, ShipPainter layer) {
        super(pointPosition, layer);
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void changeSlotAngle(double degrees) {
        setAngle(degrees);
    }

}
