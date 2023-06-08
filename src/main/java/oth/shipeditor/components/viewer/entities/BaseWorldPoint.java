package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class BaseWorldPoint implements WorldPoint{

    private static Point2D viewerCursor = new Point2D.Double();

    @Getter
    private final Point2D position;

    @Getter
    private final Painter painter;

    @Getter
    private boolean cursorInBounds;

    @Getter @Setter
    private boolean selected;

    static {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                viewerCursor = checked.rawCursor();
            }
        });
    }

    public BaseWorldPoint() {
        this(new Point2D.Double());
    }

    BaseWorldPoint(Point2D pointPosition) {
        this.position = pointPosition;
        this.painter = this.getPointPainter();
    }

    protected boolean checkIsHovered(Shape[] paintParts) {
        for (Shape part: paintParts) {
            if (part.contains(viewerCursor)) {
                return true;
            }
        }
        return false;
    }

    protected Shape createWorldConstantPaintPart(AffineTransform worldToScreen) {
        float radius = 0.25f;
        Shape dot = new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
        return worldToScreen.createTransformedShape(dot);
    }

    protected RectangularShape createScreenConstantPaintPart(AffineTransform worldToScreen) {
        Point2D point = new Point2D.Double(position.getX(), position.getY());
        Point2D dest = worldToScreen.transform(point, null);
        float radius = 6.0f;
        double destX = dest.getX();
        double destY = dest.getY();
        return new Ellipse2D.Double((int) destX - radius, (int) destY - radius,
                radius * 2, radius * 2);
    }

    protected Color createHoverColor() {
        return new Color(0xBFFFFFFF, true);
    }

    protected Color createSelectColor() {
        return new Color(0xBFFF0000, true);
    }

    protected Color createBaseColor() {
        return new Color(0xBF000000, true);
    }

    public Painter getPointPainter() {
        return (g, worldToScreen, w, h) -> {
            Paint old = g.getPaint();
            Shape inner = createWorldConstantPaintPart(worldToScreen);
            RectangularShape outer = createScreenConstantPaintPart(worldToScreen);

            this.cursorInBounds = checkIsHovered(new Shape[]{inner, outer});
            if (this.selected) {
                g.setPaint(createSelectColor());
            } else if (this.cursorInBounds) {
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

}
