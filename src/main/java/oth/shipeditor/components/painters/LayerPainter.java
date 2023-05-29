package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.FeaturePoint;
import oth.shipeditor.components.entities.WorldPoint;
import oth.shipeditor.representation.ShipLayer;
import oth.shipeditor.representation.data.Hull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public class LayerPainter implements Painter {

    @Getter @Setter
    private Point2D anchorOffset = new Point2D.Double(0, 0);

    private ShipLayer parentLayer;

    @Getter @Setter
    private Point2D translatedCenter;

    @Getter
    private BufferedImage shipSprite;

    public LayerPainter(ShipLayer layer) {
        this.parentLayer = layer;
        this.shipSprite = layer.getShipSprite();
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(ShipLayerUpdated.class, event -> {
            if (event.updated() == LayerPainter.this.parentLayer) {
                LayerPainter.this.shipSprite = event.updated().getShipSprite();
            }
        });
    }

    public Point getSpriteCenter() {
        return new Point(shipSprite.getWidth() / 2, shipSprite.getHeight() / 2);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        AffineTransform oldAT = g.getTransform();
        g.transform(worldToScreen);
        int width = shipSprite.getWidth();
        int height = shipSprite.getHeight();
        g.drawImage(shipSprite, (int) anchorOffset.getX(),
                (int) anchorOffset.getY(), width, height, null);
        g.setTransform(oldAT);
    }

    public void initialize(ShipViewerPanel viewerPanel, ShipLayer layer) {
        Hull hull = layer.getShipData().getHull();
        Point2D anchor = viewerPanel.getShipCenterAnchor();
        translatedCenter = new Point2D.Double(hull.getCenter().x - anchor.getX(),
                -hull.getCenter().y + anchor.getY());

        WorldPoint shipCenter = createShipCenterPoint(translatedCenter);
        viewerPanel.getPointsPainter().addPoint(shipCenter);
        for (Point2D.Double bound : hull.getBounds()) {
            BoundPoint boundPoint = this.createTranslatedBound(bound, translatedCenter);
            viewerPanel.getPointsPainter().addPoint(boundPoint);
        }
    }

    private BoundPoint createTranslatedBound(Point2D bound, Point2D translatedCenter) {
        double translatedX = bound.getY() + translatedCenter.getX();
        double translatedY = -bound.getX() + translatedCenter.getY();
        return new BoundPoint(new Point2D.Double(translatedX, translatedY));
    }

    private WorldPoint createShipCenterPoint(Point2D translatedCenter) {
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
                return "ShipCenter";
            }
        };
    }

}
