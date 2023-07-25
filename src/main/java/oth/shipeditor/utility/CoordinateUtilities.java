package oth.shipeditor.utility;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 24.07.2023
 */
public final class CoordinateUtilities {

    private CoordinateUtilities() {
    }

    /**
     * Rotates point 90 degrees counterclockwise around specified center point.
     * @param input point to be rotated.
     * @param translatedCenter center point around which the rotation is performed.
     * @return new {@code Point2D} representing the rotated point.
     */
    public static Point2D rotatePointByCenter(Point2D input, Point2D translatedCenter) {
        double translatedX = -input.getY() + translatedCenter.getX();
        double translatedY = -input.getX() + translatedCenter.getY();
        return new Point2D.Double(translatedX, translatedY);
    }

    public static Point2D rotateHullCenter(Point2D hullCenter, Point2D anchor) {
        double anchorX = anchor.getX();
        double anchorY = anchor.getY();
        return new Point2D.Double(hullCenter.getX() + anchorX, -hullCenter.getY() + anchorY);
    }

}
