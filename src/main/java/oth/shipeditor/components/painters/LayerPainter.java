package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.ShipViewable;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.ShipCenterPoint;
import oth.shipeditor.representation.ShipLayer;
import oth.shipeditor.representation.data.Hull;
import oth.shipeditor.representation.data.ShipData;

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

    private ShipCenterPoint centerPoint;

    @Getter
    private BufferedImage shipSprite;

    private final ShipViewable viewer;

    private boolean initialized = false;

    public LayerPainter(ShipLayer layer, ShipViewable viewable) {
        this.viewer = viewable;
        this.parentLayer = layer;
        this.shipSprite = layer.getShipSprite();
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                if (checked.updated() != this.parentLayer) return;
                if (this.parentLayer.getShipSprite() != null) {
                    this.shipSprite = this.parentLayer.getShipSprite();
                }
                if (this.parentLayer.getShipData() != null && !this.initialized) {
                    this.initialize();
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

    private void initialize() {
        ShipData shipData = this.parentLayer.getShipData();
        Hull hull = shipData.getHull();
        LayerPainter selectedLayer = this.viewer.getSelectedLayer();
        Point2D anchor = selectedLayer.getCenterAnchor();
        Point2D.Double hullCenter = hull.getCenter();
        double anchorX = anchor.getX();
        double anchorY = anchor.getY();
        Point2D.Double translatedCenter = new Point2D.Double(hullCenter.x - anchorX,
                -hullCenter.y + anchorY);
        this.centerPoint = createShipCenterPoint(translatedCenter);
        WorldPointsPainter miscPointsPainter = this.viewer.getMiscPointsPainter();
        miscPointsPainter.addPoint(this.centerPoint);
        for (Point2D.Double bound : hull.getBounds()) {
            BoundPoint boundPoint = LayerPainter.createTranslatedBound(bound, translatedCenter);
            BoundPointsPainter boundsPainter = this.viewer.getBoundsPainter();
            boundsPainter.addPoint(boundPoint);
        }
        this.initialized = true;
        this.viewer.repaintView();
    }

    private static BoundPoint createTranslatedBound(Point2D bound, Point2D translatedCenter) {
        double translatedX = bound.getY() + translatedCenter.getX();
        double translatedY = -bound.getX() + translatedCenter.getY();
        return new BoundPoint(new Point2D.Double(translatedX, translatedY));
    }

    private static ShipCenterPoint createShipCenterPoint(Point2D translatedCenter) {
        return new ShipCenterPoint(translatedCenter);
    }

}
