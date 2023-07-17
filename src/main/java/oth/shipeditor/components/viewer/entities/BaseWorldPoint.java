package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundsPanelRepaintQueued;
import oth.shipeditor.communication.events.components.CentersPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.layers.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetConfirmed;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Log4j2
public class BaseWorldPoint implements WorldPoint {

    private static Point2D viewerCursor = new Point2D.Double();

    private static CoordsDisplayMode coordsMode = CoordsDisplayMode.WORLD;

    /**
     * All points need a static reference to layer in order to streamline multiple coordinate systems functionality.
     */
    private static LayerPainter selectedLayer;

    @Getter @Setter
    private LayerPainter parentLayer;

    @Getter
    private static InstrumentMode instrumentationMode = InstrumentTabsPane.getCurrentMode();

    @Getter
    private final Point2D position;

    @Getter
    private final Painter painter;

    private final LabelPainter coordsLabel;

    @Getter
    private boolean cursorInBounds;

    @Getter @Setter
    private boolean selected;

    /**
     * Note: this method needs to be called as soon as possible when initializing the viewer.
     */
    public static void initStaticListening() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerShipDataInitialized checked) {
                selectedLayer = checked.source();

            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                if (checked.selected() == null) return;
                ShipLayer shipLayer = checked.selected();
                selectedLayer = shipLayer.getPainter();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                viewerCursor = checked.rawCursor();
            }
        });
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
        this.painter = this.getPointPainter();
        coordsLabel = createCoordsLabelPainter();
        if (layer != null) {
            this.initLayerListening();
        }
    }

    private LabelPainter createCoordsLabelPainter() {
        LabelPainter labelPainter = new LabelPainter();
        Point2D coords = this.getPosition();
        labelPainter.setLabelLocation(coords.getX(), coords.getY());
        Font font = Utility.getOrbitron(16).deriveFont(0.25f);
        labelPainter.setFont(font);
        this.adjustLabelPosition(labelPainter);
        return labelPainter;
    }

    protected void adjustLabelPosition(LabelPainter labelPainter) {
        labelPainter.setLabelAnchor(-0.25f, 0.55f);
    }

    void paintCoordsLabel(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Point2D coordsPoint = getPosition();
        Point2D toDisplay = this.getCoordinatesForDisplay();
        coordsLabel.setLabelLocation(coordsPoint.getX(), coordsPoint.getY());
        String coords = getNameForLabel() + " (" + toDisplay.getX() + ", " + toDisplay.getY() + ")";
        coordsLabel.paint(g, worldToScreen, w, h,coords);
    }

    public String getNameForLabel() {
        return "Point";
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

    private static boolean checkIsHovered(Shape[] paintParts) {
        for (Shape part: paintParts) {
            if (part.contains(viewerCursor)) {
                return true;
            }
        }
        return false;
    }

    private Shape createWorldConstantPaintPart(AffineTransform worldToScreen) {
        Shape dot = Utility.createCircle(this.position, 0.25f);
        return worldToScreen.createTransformedShape(dot);
    }

    private RectangularShape createScreenConstantPaintPart(AffineTransform worldToScreen) {
        Point2D point = new Point2D.Double(position.getX(), position.getY());
        Point2D dest = worldToScreen.transform(point, null);
        float radius = 6.0f;
        return Utility.createCircle(dest, radius);
    }

    protected Color createHoverColor() {
        return new Color(0xBFFFFFFF, true);
    }

    private static Color createSelectColor() {
        return new Color(0xBFFF0000, true);
    }

    protected Color createBaseColor() {
        return new Color(0xBF000000, true);
    }

    protected boolean isInteractable() {
        return instrumentationMode == getAssociatedMode() && parentLayer.isLayerActive();
    }

    public Painter getPointPainter() {
        return (g, worldToScreen, w, h) -> {
            Paint old = g.getPaint();
            Shape inner = createWorldConstantPaintPart(worldToScreen);
            RectangularShape outer = createScreenConstantPaintPart(worldToScreen);

            this.cursorInBounds = BaseWorldPoint.checkIsHovered(new Shape[]{inner, outer});
            if (this.selected && isInteractable()) {
                g.setPaint(BaseWorldPoint.createSelectColor());
            } else if (this.cursorInBounds && isInteractable()) {
                g.setPaint(createHoverColor());
            } else {
                g.setPaint(createBaseColor());
            }
            g.fill(inner);

            int x = (int) outer.getX();
            int y = (int) outer.getY();
            int width = (int) outer.getWidth();
            int height = (int) outer.getHeight();
            g.drawOval(x, y, width, height);
            g.setPaint(old);
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
        LayerPainter layer = BaseWorldPoint.selectedLayer;
        if (layer == null) {
            return result;
        }
        double positionX = pointPosition.getX();
        double positionY = pointPosition.getY();
        switch (coordsMode) {
            case WORLD -> {}
            case SPRITE_CENTER -> {
                Point2D center = layer.getSpriteCenter();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(positionX - centerX, positionY - centerY);
            }
            case SHIPCENTER_ANCHOR -> {
                Point2D center = layer.getCenterAnchor();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(positionX - centerX, (-positionY + centerY));
            }
            // This case uses different coordinate system alignment to be consistent with game files.
            // Otherwise, user might be confused as shown point coordinates won't match with those in file.
            case SHIP_CENTER -> {
                BaseWorldPoint shipCenter = layer.getShipCenter();
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
