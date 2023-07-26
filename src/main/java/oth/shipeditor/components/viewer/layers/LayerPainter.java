package oth.shipeditor.components.viewer.layers;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.LayerAnchorDragged;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerRemovalConfirmed;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.components.viewer.PrimaryShipViewer;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.points.CenterPointPainter;
import oth.shipeditor.components.viewer.painters.points.ShieldPointPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.utility.CoordinateUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Distinct from parent ship layer instance: present class has to do with direct visual representation.
 * Painter instance is not concerned with loading and file interactions, and leaves that to other classes.
 * @author Ontheheavens
 * @since 29.05.2023
 */
@SuppressWarnings({"ClassWithTooManyFields", "OverlyCoupledClass"})
@Log4j2
public final class LayerPainter implements Painter {

    @Getter
    private final BoundPointsPainter boundsPainter;
    @Getter
    private final CenterPointPainter centerPointPainter;

    @Getter
    private final ShieldPointPainter shieldPointPainter;

    /**
     * Convenience collection for bulk manipulation of layer painters.
     */
    @Getter
    private final List<AbstractPointPainter> allPainters;

    @Getter
    private Point2D anchorOffset = new Point2D.Double(0, 0);

    @Getter
    private float spriteOpacity = 1.0f;

    /**
     * Reference to parent layer is needed here for points cleanup.
     */
    @Getter
    private final ShipLayer parentLayer;

    private final PrimaryShipViewer viewer;

    @Getter
    private BufferedImage shipSprite;

    @Getter
    private boolean uninitialized = true;

    private final List<BusEventListener> listeners;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public LayerPainter(ShipLayer layer, PrimaryShipViewer viewerPanel) {
        this.parentLayer = layer;
        this.viewer = viewerPanel;
        this.listeners = new ArrayList<>();

        this.centerPointPainter = new CenterPointPainter(this);
        this.shieldPointPainter = new ShieldPointPainter(this);
        this.boundsPainter = new BoundPointsPainter(this);

        this.allPainters = new ArrayList<>();
        allPainters.add(centerPointPainter);
        allPainters.add(shieldPointPainter);
        allPainters.add(boundsPainter);
        this.shipSprite = layer.getShipSprite();
        this.initPainterListeners(layer);
        this.initLayerListeners();
    }

    public boolean isLayerActive() {
        LayerManager layerManager = viewer.getLayerManager();
        return (layerManager.getActiveLayer() == this.parentLayer);
    }

    private void cleanupForRemoval() {
        for (AbstractPointPainter pointPainter : this.getAllPainters()) {
            LayerPainter.clearPointPainter(pointPainter);
        }
        listeners.forEach(EventBus::unsubscribe);
    }

    private void initLayerListeners() {
        BusEventListener removalListener = event -> {
            if (event instanceof ShipLayerRemovalConfirmed checked) {
                if (checked.removed() != this.parentLayer) return;
                this.cleanupForRemoval();
            }
        };
        listeners.add(removalListener);
        EventBus.subscribe(removalListener);
        BusEventListener anchorDragListener = event -> {
            if (event instanceof LayerAnchorDragged checked && checked.selected() == this) {
                AffineTransform screenToWorld = checked.screenToWorld();
                Point2D difference = checked.difference();
                Point2D wP = screenToWorld.transform(difference, null);
                double roundedX = Math.round(wP.getX() * 2) / 2.0;
                double roundedY = Math.round(wP.getY() * 2) / 2.0;
                Point2D corrected = new Point2D.Double(roundedX, roundedY);
                updateAnchorOffset(corrected);
                Events.repaintView();
            }
        };
        listeners.add(anchorDragListener);
        EventBus.subscribe(anchorDragListener);
    }

    private static void clearPointPainter(AbstractPointPainter pointPainter) {
        Iterable<BaseWorldPoint> points = new ArrayList<>(pointPainter.getPointsIndex());
        for (BaseWorldPoint point : points) {
            point.cleanupForRemoval();
            pointPainter.removePoint(point);
        }
        pointPainter.cleanupForRemoval();
    }

    private void initPainterListeners(ShipLayer layer) {
        BusEventListener layerUpdateListener = event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                if (checked.updated() != layer) return;
                if (layer.getShipSprite() != null) {
                    this.shipSprite = layer.getShipSprite();
                }
                if (layer.getShipData() != null && this.uninitialized) {
                    this.initializeShipData(layer.getShipData());
                }
            }
        };
        listeners.add(layerUpdateListener);
        EventBus.subscribe(layerUpdateListener);
    }

    public void updateAnchorOffset(Point2D updated) {
        Point2D oldOffset = this.anchorOffset;
        Point2D difference = new Point2D.Double(oldOffset.getX() - updated.getX(),
                oldOffset.getY() - updated.getY());
        EventBus.publish(new AnchorOffsetQueued(this, difference));
        this.anchorOffset = updated;
    }

    public ShipCenterPoint getShipCenter() {
        return this.centerPointPainter.getCenterPoint();
    }

    public Point2D getCenterAnchor() {
        return new Point2D.Double( anchorOffset.getX(), anchorOffset.getY() + shipSprite.getHeight());
    }

    public Point2D getSpriteCenter() {
        return new Point2D.Double((anchorOffset.getX() + shipSprite.getWidth() / 2.0f),
                (anchorOffset.getY() + shipSprite.getHeight() / 2.0f));
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        AffineTransform oldAT = g.getTransform();
        g.transform(worldToScreen);
        int width = shipSprite.getWidth();
        int height = shipSprite.getHeight();
        int rule = AlphaComposite.SRC_OVER;
        float alpha = this.spriteOpacity;
        Composite old = g.getComposite();
        Composite opacity = AlphaComposite.getInstance(rule, alpha) ;
        g.setComposite(opacity);
        g.drawImage(shipSprite, (int) anchorOffset.getX(),
                (int) anchorOffset.getY(), width, height, null);
        g.setComposite(old);
        g.setTransform(oldAT);
    }

    void setSpriteOpacity(float opacity) {
        if (opacity < 0.0f) {
            this.spriteOpacity = 0.0f;
        } else this.spriteOpacity = Math.min(opacity, 1.0f);
    }

    private void initializeShipData(ShipData shipData) {
        Hull hull = shipData.getHull();

        Point2D anchor = this.getCenterAnchor();
        Point2D hullCenter = hull.getCenter();

        Point2D translatedCenter = CoordinateUtilities.rotateHullCenter(hullCenter, anchor);

        this.centerPointPainter.initCenterPoint(translatedCenter, hull);

        shipData.initHullStyle();

        Point2D shieldCenter = hull.getShieldCenter();

        Point2D shieldCenterTranslated = CoordinateUtilities.rotatePointByCenter(shieldCenter, translatedCenter);
        this.shieldPointPainter.initShieldPoint(shieldCenterTranslated, shipData);

        Stream<Point2D> boundStream = Arrays.stream(hull.getBounds());
        boundStream.forEach(bound -> {
            Point2D rotatedPosition = CoordinateUtilities.rotatePointByCenter(bound, translatedCenter);
            BoundPoint boundPoint = new BoundPoint(rotatedPosition, this);
            boundsPainter.addPoint(boundPoint);
        });

        this.uninitialized = false;
        log.info("{} initialized!", this);
        EventBus.publish(new LayerShipDataInitialized(this));
        EventBus.publish(new ViewerRepaintQueued());
    }

    @Override
    public String toString() {
        return "Layer Painter #" + this.hashCode();
    }

}
