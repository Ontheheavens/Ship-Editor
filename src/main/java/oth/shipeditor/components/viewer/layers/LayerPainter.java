package oth.shipeditor.components.viewer.layers;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.PainterAdditionQueued;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.viewer.ShipViewerPanel;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.painters.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.BoundPointsPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public class LayerPainter implements Painter {

    @Getter
    private final BoundPointsPainter boundsPainter;

    private final AbstractPointPainter hullPointsPainter;

    @Getter @Setter
    private Point2D anchorOffset = new Point2D.Double(0, 0);

    private ShipCenterPoint centerPoint;

    @Getter
    private BufferedImage shipSprite;

    private boolean uninitialized = true;

    public LayerPainter(ShipLayer parentLayer, ShipViewerPanel viewerPanel) {
        this.hullPointsPainter = this.createHullPointsPainter();
        this.boundsPainter = new BoundPointsPainter(viewerPanel);
        this.shipSprite = parentLayer.getShipSprite();
        this.initListeners(parentLayer);
    }

    private void initListeners(ShipLayer parentLayer) {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                if (checked.updated() != parentLayer) return;
                if (parentLayer.getShipSprite() != null) {
                    this.shipSprite = parentLayer.getShipSprite();
                }
                if (parentLayer.getShipData() != null && this.uninitialized) {
                    this.initialize(parentLayer.getShipData());
                }
            }
        });
    }

    public ShipCenterPoint getShipCenter() {
        return this.centerPoint;
    }

    public Point2D getCenterAnchor() {
        return new Point2D.Double( anchorOffset.getX(), shipSprite.getHeight());
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

    private AbstractPointPainter createHullPointsPainter() {
        return new HullPointsPainter();
    }

    private void initialize(ShipData shipData) {
        Hull hull = shipData.getHull();
        Point2D anchor = this.getCenterAnchor();
        Point2D.Double hullCenter = hull.getCenter();
        double anchorX = anchor.getX();
        double anchorY = anchor.getY();
        Point2D.Double translatedCenter = new Point2D.Double(hullCenter.x - anchorX,
                -hullCenter.y + anchorY);
        this.centerPoint = LayerPainter.createShipCenterPoint(translatedCenter);
        hullPointsPainter.addPoint(this.centerPoint);
        for (Point2D.Double bound : hull.getBounds()) {
            BoundPoint boundPoint = LayerPainter.createTranslatedBound(bound, translatedCenter);
            boundsPainter.addPoint(boundPoint);
        }
        this.uninitialized = false;
        EventBus.publish(new PainterAdditionQueued(hullPointsPainter, 4));
        EventBus.publish(new PainterAdditionQueued(boundsPainter, 4));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private static BoundPoint createTranslatedBound(Point2D bound, Point2D translatedCenter) {
        double translatedX = bound.getY() + translatedCenter.getX();
        double translatedY = -bound.getX() + translatedCenter.getY();
        return new BoundPoint(new Point2D.Double(translatedX, translatedY));
    }

    private static ShipCenterPoint createShipCenterPoint(Point2D translatedCenter) {
        return new ShipCenterPoint(translatedCenter);
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    private class HullPointsPainter extends AbstractPointPainter {
        private final List<BaseWorldPoint> points = new ArrayList<>();
        @Override
        protected List<BaseWorldPoint> getPointsIndex() {
            return points;
        }

        @Override
        protected void addPointToIndex(BaseWorldPoint point) {
            points.add(point);
        }

        @Override
        protected void removePointFromIndex(BaseWorldPoint point) {
            points.remove(point);
        }

        @Override
        protected BaseWorldPoint getTypeReference() {
            return new BaseWorldPoint();
        }
    }

}
