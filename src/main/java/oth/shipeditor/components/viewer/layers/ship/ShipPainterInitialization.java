package oth.shipeditor.components.viewer.layers.ship;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.painters.points.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.points.CenterPointPainter;
import oth.shipeditor.components.viewer.painters.points.ShieldPointPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
@Log4j2
public final class ShipPainterInitialization {

    private ShipPainterInitialization() {
    }

    static void initializeShipData(ShipPainter shipPainter, ShipData shipData) {
        Hull hull = shipData.getHull();

        Point2D anchor = shipPainter.getCenterAnchor();
        Point2D hullCenter = hull.getCenter();

        Point2D translatedCenter = ShipPainterInitialization.rotateHullCenter(hullCenter, anchor);

        ShipPainterInitialization.initCentroids(shipPainter, shipData, translatedCenter);

        ShipPainterInitialization.initBounds(shipPainter, hull, translatedCenter);

        shipPainter.setUninitialized(false);
        log.info("{} initialized!", shipPainter);
        EventBus.publish(new LayerShipDataInitialized(shipPainter));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private static void initCentroids(ShipPainter shipPainter, ShipData shipData, Point2D translatedCenter) {
        Hull hull = shipData.getHull();

        CenterPointPainter centerPointPainter = shipPainter.getCenterPointPainter();
        centerPointPainter.initCenterPoint(translatedCenter, hull);

        shipData.initHullStyle();

        Point2D shieldCenter = hull.getShieldCenter();

        Point2D shieldCenterTranslated = ShipPainterInitialization.rotatePointByCenter(shieldCenter, translatedCenter);
        ShieldPointPainter shieldPointPainter = shipPainter.getShieldPointPainter();
        shieldPointPainter.initShieldPoint(shieldCenterTranslated, shipData);
    }

    private static void initBounds(ShipPainter shipPainter, Hull hull, Point2D translatedCenter) {
        Stream<Point2D> boundStream = Arrays.stream(hull.getBounds());
        BoundPointsPainter boundsPainter = shipPainter.getBoundsPainter();
        boundStream.forEach(bound -> {
            Point2D rotatedPosition = ShipPainterInitialization.rotatePointByCenter(bound, translatedCenter);
            BoundPoint boundPoint = new BoundPoint(rotatedPosition, shipPainter);
            boundsPainter.addPoint(boundPoint);
        });
    }

    /**
     * Rotates point 90 degrees counterclockwise around specified center point.
     * @param input point to be rotated.
     * @param translatedCenter center point around which the rotation is performed.
     * @return new {@code Point2D} representing the rotated point.
     */
    private static Point2D rotatePointByCenter(Point2D input, Point2D translatedCenter) {
        double translatedX = -input.getY() + translatedCenter.getX();
        double translatedY = -input.getX() + translatedCenter.getY();
        return new Point2D.Double(translatedX, translatedY);
    }

    private static Point2D rotateHullCenter(Point2D hullCenter, Point2D anchor) {
        double anchorX = anchor.getX();
        double anchorY = anchor.getY();
        return new Point2D.Double(hullCenter.getX() + anchorX, -hullCenter.getY() + anchorY);
    }

}
