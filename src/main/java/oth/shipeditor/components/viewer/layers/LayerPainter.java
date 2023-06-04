package oth.shipeditor.components.viewer.layers;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerPaintersInitialized;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerRemovalConfirmed;
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
 * Distinct from parent ship layer instance: present class has to do with direct visual representation.
 * Painter instance is not concerned with loading and file interactions, and leaves that to other classes.
 * @author Ontheheavens
 * @since 29.05.2023
 */
public class LayerPainter implements Painter {

    @Getter
    private final BoundPointsPainter boundsPainter;
    @Getter
    private final HullPointsPainter hullPointsPainter;

    /**
     * Convenience collection for bulk manipulation of layer painters.
     */
    @Getter
    private final List<Painter> allPainters;

    @Getter @Setter
    private Point2D anchorOffset = new Point2D.Double(0, 0);

    private ShipCenterPoint centerPoint;

    /**
     * Reference to parent layer is needed here for points cleanup.
     */
    @Getter
    private final ShipLayer parentLayer;

    @Getter
    private BufferedImage shipSprite;

    private boolean uninitialized = true;

    public LayerPainter(ShipLayer layer, ShipViewerPanel viewerPanel) {
        this.parentLayer = layer;
        this.hullPointsPainter = this.createHullPointsPainter();
        this.boundsPainter = new BoundPointsPainter(viewerPanel);
        this.allPainters = new ArrayList<>();
        allPainters.add(hullPointsPainter);
        allPainters.add(boundsPainter);
        this.shipSprite = layer.getShipSprite();
        this.initPainterListeners(layer);
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerRemovalConfirmed checked) {
                if (checked.removed() != this.parentLayer) return;
                this.clearBoundPainter();
                this.clearHullPainter();
            }
        });
    }

    private void clearHullPainter() {
        Iterable<BaseWorldPoint> hullPoints = new ArrayList<>(this.hullPointsPainter.getPointsIndex());
        for (BaseWorldPoint point : hullPoints) {
            this.hullPointsPainter.removePoint(point);
        }
    }

    private void clearBoundPainter() {
        Iterable<BoundPoint> bounds = new ArrayList<>(this.boundsPainter.getBoundPoints());
        for (BoundPoint point : bounds) {
            this.boundsPainter.removePoint(point);
        }
    }

    private void initPainterListeners(ShipLayer layer) {
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                if (checked.updated() != layer) return;
                if (layer.getShipSprite() != null) {
                    this.shipSprite = layer.getShipSprite();
                }
                if (layer.getShipData() != null && this.uninitialized) {
                    this.initialize(layer.getShipData());
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

    private HullPointsPainter createHullPointsPainter() {
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
        EventBus.publish(new LayerPaintersInitialized(this, 4));
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
