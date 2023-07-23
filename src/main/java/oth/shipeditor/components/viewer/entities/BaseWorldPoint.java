package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundsPanelRepaintQueued;
import oth.shipeditor.communication.events.components.CentersPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetConfirmed;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.TextPainter;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class BaseWorldPoint implements WorldPoint {

    private static CoordsDisplayMode coordsMode = CoordsDisplayMode.WORLD;

    @Getter @Setter
    private LayerPainter parentLayer;

    @Getter
    private static InstrumentMode instrumentationMode = InstrumentTabsPane.getCurrentMode();

    @Getter
    private final Point2D position;

    @Getter
    private final Painter painter;

    private final TextPainter coordsLabel;

    @Getter @Setter
    private boolean cursorInBounds;

    @Getter @Setter
    private boolean selected;

    @Getter
    private final AffineTransform delegateWorldToScreen;

    /**
     * Note: this method needs to be called as soon as possible when initializing the viewer.
     */
    public static void initStaticListening() {
        EventBus.subscribe(event -> {
            if (event instanceof CoordsModeChanged checked) {
                coordsMode = checked.newMode();
                EventBus.publish(new BoundsPanelRepaintQueued());
                EventBus.publish(new CentersPanelRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                instrumentationMode = checked.newMode();
            }
        });
    }

    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.LAYER;
    }

    public BaseWorldPoint() {
        this(new Point2D.Double());
    }

    public BaseWorldPoint(Point2D pointPosition) {
        this(new Point2D.Double(pointPosition.getX(), pointPosition.getY()), null);

    }

    public BaseWorldPoint(Point2D pointPosition, LayerPainter layer) {
        this.position = new Point2D.Double(pointPosition.getX(), pointPosition.getY());
        this.parentLayer = layer;
        this.delegateWorldToScreen = new AffineTransform();
        this.painter = this.createPointPainter();
        this.coordsLabel = new TextPainter();
        if (layer != null) {
            this.initLayerListening();
        }
    }

    void paintCoordsLabel(Graphics2D g, AffineTransform worldToScreen) {
        Point2D coordsPoint = getPosition();
        Point2D toDisplay = this.getCoordinatesForDisplay();

        DrawUtilities.drawWithConditionalOpacity(g, graphics2D -> {
            String coords = getNameForLabel() + " (" + toDisplay.getX() + ", " + toDisplay.getY() + ")";

            coordsLabel.setWorldPosition(coordsPoint);
            coordsLabel.setText(coords);
            coordsLabel.paintText(graphics2D, worldToScreen);
        });
    }

    public String getNameForLabel() {
        return "Point";
    }

    public String getPositionText() {
        Point2D location = this.getCoordinatesForDisplay();
        return "[" + location.getX() + "," + location.getY() + "]";
    }
    
    private void initLayerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof AnchorOffsetQueued checked && checked.layer() == this.parentLayer) {
                Point2D offset = checked.difference();
                Point2D oldBoundPosition = this.getPosition();
                this.setPosition(oldBoundPosition.getX() - offset.getX(),
                        oldBoundPosition.getY() - offset.getY());
                EventBus.publish(new AnchorOffsetConfirmed(this, offset));
            }
        });
    }

    protected Color createHoverColor() {
        return ColorUtilities.getBlendedColor(createBaseColor(), createSelectColor(),0.5);
    }

    @SuppressWarnings("WeakerAccess")
    protected Color createSelectColor() {
        return new Color(0xFFFF0000, true);
    }

    protected Color createBaseColor() {
        return new Color(0xFFFFFFFF, true);
    }


    protected boolean isInteractable() {
        LayerPainter layer = getParentLayer();
        if (layer == null) {
            return true;
        }
        return BaseWorldPoint.getInstrumentationMode() == getAssociatedMode() && layer.isLayerActive();
    }


    @SuppressWarnings("WeakerAccess")
    protected Color getCurrentColor() {
        Color result;
        if (this.selected && isInteractable()) {
            result = createSelectColor();
        } else if (this.cursorInBounds && isInteractable()) {
            result = createHoverColor();
        } else {
            result = createBaseColor();
        }
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public static Shape getShapeForPoint(AffineTransform worldToScreen, Point2D position) {
        Shape circle = ShapeUtilities.createCircle(position, 0.10f);

        return ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, circle, 12);
    }

    public Painter createPointPainter() {
        return (g, worldToScreen, w, h) -> {
            Shape circle = BaseWorldPoint.getShapeForPoint(worldToScreen, this.position);

            this.cursorInBounds = StaticController.checkIsHovered(circle);

            DrawUtilities.outlineShape(g, circle, Color.BLACK, 2);
            DrawUtilities.fillShape(g, circle, getCurrentColor());
        };
    }

    public void setPosition(double x, double y) {
        this.position.setLocation(x, y);
    }

    public void setPosition(Point2D input) {
        this.position.setLocation(input.getX(), input.getY());
    }

    public Point2D getCoordinatesForDisplay() {
        Point2D pointPosition = this.getPosition();
        Point2D result = pointPosition;
        ShipLayer activeLayer = StaticController.getActiveLayer();
        if (activeLayer == null) {
            return result;
        }
        LayerPainter layerPainter = activeLayer.getPainter();
        if (layerPainter == null || layerPainter.isUninitialized()) {
            return result;
        }
        double positionX = pointPosition.getX();
        double positionY = pointPosition.getY();
        switch (coordsMode) {
            case WORLD -> {}
            case SPRITE_CENTER -> {
                Point2D center = layerPainter.getSpriteCenter();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(positionX - centerX, positionY - centerY);
            }
            case SHIPCENTER_ANCHOR -> {
                Point2D center = layerPainter.getCenterAnchor();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(positionX - centerX, (-positionY + centerY));
            }
            // This case uses different coordinate system alignment to be consistent with game files.
            // Otherwise, user might be confused as shown point coordinates won't match with those in file.
            case SHIP_CENTER -> {
                BaseWorldPoint shipCenter = layerPainter.getShipCenter();
                Point2D center = shipCenter.position;
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(-(positionY - centerY), -(positionX - centerX));
            }
        }
        if (result.getX() == -0.0) {
            result.setLocation(0, result.getY());
        }
        if (result.getY() == -0.0) {
            result.setLocation(result.getX(), 0);
        }
        return result;
    }

}
