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
    private boolean cursorInBounds = false;

    @Getter @Setter
    private boolean selected = false;

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

    public BaseWorldPoint(Point2D position) {
        this.position = position;
        this.painter = this.getPointPainter();
    }

    public Painter getPointPainter() {
        return new Painter() {
            final Point2D point = position;
            @Override
            public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
                Paint old = g.getPaint();
                Ellipse2D dot = new Ellipse2D.Double(point.getX() - 0.25, point.getY() - 0.25, 0.5, 0.5);
                Shape result = worldToScreen.createTransformedShape(dot);

                Point2D dest = worldToScreen.transform(point, null);
                double destX = dest.getX();
                double destY = dest.getY();
                Ellipse2D outer = new Ellipse2D.Double((int) destX - 6, (int) destY - 6, 12, 12);

                BaseWorldPoint.this.cursorInBounds = outer.contains(viewerCursor) || result.contains(viewerCursor);
                if (BaseWorldPoint.this.selected) {
                    g.setPaint(new Color(0xBFFF0000, true));
                } else if (BaseWorldPoint.this.cursorInBounds) {
                    g.setPaint(new Color(0xBFFFFFFF, true));
                } else {
                    g.setPaint(new Color(0xBF000000, true));
                }
                g.fill(result);

                int x = (int) outer.getX();
                int y = (int) outer.getY();
                int width = (int) outer.getWidth();
                int height = (int) outer.getHeight();
                g.drawOval(x, y, width, height);
                g.setPaint(old);
            }
        };
    }

    public void setPosition(double x, double y) {
        this.position.setLocation(x, y);
    }

}
