package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.ShipCenterPoint;
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

    private final ShipLayer parentLayer;

    @Getter @Setter
    private Point2D translatedCenter;

    @Getter
    private BufferedImage shipSprite;

    private final ShipViewerPanel viewer;

    private boolean initialized = false;

    public LayerPainter(ShipLayer layer, ShipViewerPanel viewer) {
        this.viewer = viewer;
        this.parentLayer = layer;
        this.shipSprite = layer.getShipSprite();
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                if (checked.updated() != LayerPainter.this.parentLayer) return;
                if (parentLayer.getShipSprite() != null) {
                    LayerPainter.this.shipSprite = parentLayer.getShipSprite();
                }
                if (parentLayer.getShipData() != null && !initialized) {
                    LayerPainter.this.initialize();
                }
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

    public void initialize() {
        Hull hull = parentLayer.getShipData().getHull();
        Point2D anchor = viewer.getShipCenterAnchor();
        translatedCenter = new Point2D.Double(hull.getCenter().x - anchor.getX(),
                -hull.getCenter().y + anchor.getY());

        WorldPoint shipCenter = createShipCenterPoint(translatedCenter);
        viewer.getPointsPainter().addPoint(shipCenter);
        for (Point2D.Double bound : hull.getBounds()) {
            BoundPoint boundPoint = this.createTranslatedBound(bound, translatedCenter);
            viewer.getBoundsPainter().addPoint(boundPoint);
        }
        initialized = true;
        viewer.repaint();
    }

    private BoundPoint createTranslatedBound(Point2D bound, Point2D translatedCenter) {
        double translatedX = bound.getY() + translatedCenter.getX();
        double translatedY = -bound.getX() + translatedCenter.getY();
        return new BoundPoint(new Point2D.Double(translatedX, translatedY));
    }

    private ShipCenterPoint createShipCenterPoint(Point2D translatedCenter) {
        return new ShipCenterPoint(translatedCenter);
    }

}
