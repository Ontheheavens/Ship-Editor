package oth.shipeditor.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.javagl.viewer.Painter;
import lombok.Getter;
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
import java.util.Arrays;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class ShipData {

    @Getter
    private Hull hull;

    @Getter
    private Point2D translatedCenter;

    public ShipData(URI uri) {
        hull = null;
        try {
            File file = new File(uri);
            ObjectMapper objectMapper = new ObjectMapper();
            hull = objectMapper.readValue(file, Hull.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (hull == null) return;
        System.out.println(Arrays.toString(hull.bounds));
        System.out.println(hull.hullName);

        ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
        PointsPainter painter = viewerPanel.getPointsPainter();

        Point2D anchor = viewerPanel.getShipCenterAnchor();
        translatedCenter = new Point2D.Double(hull.center.x - anchor.getX(),
                -hull.center.y + anchor.getY());

        WorldPoint shipCenter = new FeaturePoint(translatedCenter) {
            @Override
            protected Painter createSecondaryPainter() {
                return (g, worldToScreen, w, h) -> {
                    Point2D center = worldToScreen.transform(getPosition(), null);
                    int x = (int) center.getX(), y = (int) center.getY(), l = 15;
                    g.drawLine(x-l, y-l, x+l, y+l);
                    g.drawLine(x-l, y+l, x+l, y-l);
                };
            }
            @Override
            public String toString() {
                Point2D translated = this.getCoordinatesForDisplay();
                return "ShipCenter {" + translated.getX() + "," + translated.getY() + '}';
            }
        };
        painter.addPoint(shipCenter);

        for (Point2D.Double bound : hull.bounds) {
            // Translate the point based on the visualisation coordinate system.
            double translatedX = bound.getY() + translatedCenter.getX();
            double translatedY = -bound.getX() + translatedCenter.getY();
            Point2D.Double translatedPoint = new Point2D.Double(translatedX, translatedY);

            // Create a BoundPoint with the translated point.
            BoundPoint boundPoint = new BoundPoint(translatedPoint);
            painter.addPoint(boundPoint);
        }
    }

}
