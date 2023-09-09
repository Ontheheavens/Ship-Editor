package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.TextPainter;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;
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
public class BaseWorldPoint implements WorldPoint, Painter {

    @Getter @Setter
    private LayerPainter parentLayer;

    @Getter
    private final Point2D position;

    @Getter
    private final TextPainter coordsLabel;

    @Getter @Setter
    private boolean cursorInBounds;

    @Getter @Setter
    private boolean pointSelected;

    @Getter
    private final AffineTransform delegateWorldToScreen;

    private BusEventListener anchorDragListener;

    @Getter @Setter
    private double paintSizeMultiplier = 1;

    public EditorInstrument getAssociatedMode() {
        return EditorInstrument.LAYER;
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
        this.coordsLabel = new TextPainter();
        if (layer != null) {
            this.initLayerListening();
        }
    }

    protected void paintCoordsLabel(Graphics2D g, AffineTransform worldToScreen) {
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
        return Utility.getPointPositionText(location);
    }
    
    private void initLayerListening() {
        anchorDragListener = event -> {
            if (event instanceof AnchorOffsetQueued checked && checked.layer() == this.parentLayer) {
                Point2D offset = checked.difference();
                Point2D oldPosition = this.getPosition();
                this.setPosition(oldPosition.getX() - offset.getX(),
                        oldPosition.getY() - offset.getY());
                UndoOverseer.adjustPointEditsOffset(this, offset);
            }
        };
        EventBus.subscribe(anchorDragListener);
    }

    public void cleanupForRemoval() {
        EventBus.unsubscribe(anchorDragListener);
    }

    Color createHoverColor() {
        return ColorUtilities.getBlendedColor(createBaseColor(), createSelectColor(),0.5);
    }

    @SuppressWarnings("WeakerAccess")
    protected Color createSelectColor() {
        return new Color(0xFFFF0000, true);
    }

    protected Color createBaseColor() {
        return new Color(0xFFFFFFFF, true);
    }


    boolean isInteractable() {
        LayerPainter layer = getParentLayer();
        if (layer == null) {
            return true;
        }
        return ShipInstrumentsPane.getCurrentMode() == getAssociatedMode() && layer.isLayerActive();
    }

    @SuppressWarnings("WeakerAccess")
    public Color getCurrentColor() {
        Color result;
        if (this.pointSelected && isInteractable()) {
            result = createSelectColor();
        } else if (this.cursorInBounds && isInteractable()) {
            result = createHoverColor();
        } else {
            result = createBaseColor();
        }
        return result;
    }

    @SuppressWarnings("SameParameterValue")
    private Shape getShapeForPoint(AffineTransform worldToScreen, float worldSize, float screenSize) {
        Shape circle = ShapeUtilities.createCircle(position, (float) (worldSize * paintSizeMultiplier));

        return ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, circle, screenSize * paintSizeMultiplier);
    }

    public void setPosition(double x, double y) {
        this.position.setLocation(x, y);
    }

    public void setPosition(Point2D input) {
        this.setPosition(input.getX(), input.getY());
    }

    public Point2D getCoordinatesForDisplay() {
        Point2D pointPosition = this.getPosition();
        return Utility.getPointCoordinatesForDisplay(pointPosition);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Shape shape = this.getShapeForPoint(worldToScreen, 0.10f, 12);

        this.cursorInBounds = StaticController.checkIsHovered(shape);

        DrawUtilities.outlineShape(g, shape, Color.BLACK, 1.5f);
        DrawUtilities.fillShape(g, shape, getCurrentColor());
    }

}
