package oth.shipeditor.components.viewer.layers;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
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
import oth.shipeditor.components.viewer.painters.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.CenterPointsPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;

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
@SuppressWarnings("ClassWithTooManyFields")
@Log4j2
public final class LayerPainter implements Painter {

    @Getter @Setter
    private int paintOrdering;

    @Getter
    private final BoundPointsPainter boundsPainter;
    @Getter
    private final CenterPointsPainter centerPointsPainter;

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

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public LayerPainter(ShipLayer layer, PrimaryShipViewer viewerPanel, int order) {
        this.paintOrdering = order;
        this.parentLayer = layer;
        this.viewer = viewerPanel;
        this.centerPointsPainter = new CenterPointsPainter(this);
        this.boundsPainter = new BoundPointsPainter(viewerPanel, this);
        this.allPainters = new ArrayList<>();
        allPainters.add(centerPointsPainter);
        allPainters.add(boundsPainter);
        this.shipSprite = layer.getShipSprite();
        this.initPainterListeners(layer);
        this.initLayerListeners();
    }

    public boolean isLayerActive() {
        LayerManager layerManager = viewer.getLayerManager();
        return (layerManager.getActiveLayer() == this.parentLayer);
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerRemovalConfirmed checked) {
                if (checked.removed() != this.parentLayer) return;
                this.clearBoundPainter();
                this.clearHullPainter();
            }
        });
        EventBus.subscribe(event -> {
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
        });
    }

    private void clearHullPainter() {
        Iterable<BaseWorldPoint> hullPoints = new ArrayList<>(centerPointsPainter.getPointsIndex());
        for (BaseWorldPoint point : hullPoints) {
            this.centerPointsPainter.removePoint(point);
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
                    this.initializeShipData(layer.getShipData());
                }
            }
        });
    }

    public void updateAnchorOffset(Point2D updated) {
        Point2D oldOffset = this.anchorOffset;
        Point2D difference = new Point2D.Double(oldOffset.getX() - updated.getX(),
                oldOffset.getY() - updated.getY());
        EventBus.publish(new AnchorOffsetQueued(this, difference));
        this.anchorOffset = updated;
    }

    public ShipCenterPoint getShipCenter() {
        return this.centerPointsPainter.getCenterPoint();
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
        Point2D.Double hullCenter = hull.getCenter();
        double anchorX = anchor.getX();
        double anchorY = anchor.getY();
        Point2D.Double translatedCenter = new Point2D.Double(hullCenter.x + anchorX,
                -hullCenter.y + anchorY);
        this.centerPointsPainter.initCenterPoint(translatedCenter, hull);
        Stream<Point2D> boundStream = Arrays.stream(hull.getBounds());
        boundStream.forEach(bound -> {
            BoundPoint boundPoint = this.createTranslatedBound(bound, translatedCenter);
            boundsPainter.addPoint(boundPoint);
        });
        this.uninitialized = false;
        log.info("{} initialized!", this);
        EventBus.publish(new LayerShipDataInitialized(this, this.paintOrdering));
        EventBus.publish(new ViewerRepaintQueued());
    }

    // TODO: streamline translation methods. Probably make separate initializing class?

    private BoundPoint createTranslatedBound(Point2D bound, Point2D translatedCenter) {
        double translatedX = -bound.getY() + translatedCenter.getX();
        double translatedY = -bound.getX() + translatedCenter.getY();
        return new BoundPoint(new Point2D.Double(translatedX, translatedY), this);
    }

    @Override
    public String toString() {
        return "Layer Painter #" + this.hashCode();
    }

}
