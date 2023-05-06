package oth.shipeditor.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.PointsPainter;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.FeaturePoint;
import oth.shipeditor.components.entities.WorldPoint;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class ShipData {

    @Getter
    private final Hull hull;

    @Getter @Setter
    private Point2D.Double translatedCenter;

    public ShipData(URI uri) {
        hull = this.loadHullFromURI(uri);
        if (hull == null) throw new RuntimeException("Failed to load hull from URI!");

        ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
        PointsPainter painter = viewerPanel.getPointsPainter();

        Point2D anchor = viewerPanel.getShipCenterAnchor();
        translatedCenter = new Point2D.Double(hull.center.x - anchor.getX(),
                -hull.center.y + anchor.getY());

        WorldPoint shipCenter = createShipCenterPoint(translatedCenter);
        painter.addPoint(shipCenter);
        for (Point2D.Double bound : hull.bounds) {
            BoundPoint boundPoint = this.createTranslatedBound(bound, translatedCenter);
            painter.addPoint(boundPoint);
        }
    }

    private BoundPoint createTranslatedBound(Point2D.Double bound, Point2D.Double translatedCenter) {
        double translatedX = bound.getY() + translatedCenter.getX();
        double translatedY = -bound.getX() + translatedCenter.getY();
        return new BoundPoint(new Point2D.Double(translatedX, translatedY));
    }

    private Hull loadHullFromURI(URI uri) {
        try {
            File file = new File(uri);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, Hull.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private WorldPoint createShipCenterPoint(Point2D.Double translatedCenter) {
        return new FeaturePoint(translatedCenter) {
            @Override
            protected Painter createSecondaryPainter() {
                return (g, worldToScreen, w, h) -> {
                    Point2D center = worldToScreen.transform(getPosition(), null);
                    int x = (int) center.getX(), y = (int) center.getY(), l = 15;
                    g.drawLine(x - l, y - l, x + l, y + l);
                    g.drawLine(x - l, y + l, x + l, y - l);
                };
            }

            @Override
            public String toString() {
                Point2D translated = this.getCoordinatesForDisplay();
                return "ShipCenter {" + translated.getX() + "," + translated.getY() + '}';
            }
        };
    }

}
