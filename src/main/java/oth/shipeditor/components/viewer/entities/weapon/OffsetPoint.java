package oth.shipeditor.components.viewer.entities.weapon;

import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 02.09.2023
 */
public class OffsetPoint extends AngledPoint {

    private double angle;

    public OffsetPoint(Point2D pointPosition, WeaponPainter layer) {
        super(pointPosition, layer);
    }

    @Override
    public void setAngle(double degrees) {
        this.angle = degrees;
    }

    @Override
    public double getAngle() {
        return this.angle;
    }

    @Override
    public void changeSlotAngle(double degrees) {
        this.setAngle(degrees);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        float worldSizeRadius = 0.15f;
        Shape shape = ShapeUtilities.createCircle(this.getPosition(), worldSizeRadius);

        Point2D position = this.getPosition();
        DrawUtilities.drawAngledCirclePointer(g, worldToScreen, shape, worldSizeRadius,
                this.angle, position, Color.WHITE);
    }

}
