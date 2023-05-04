package oth.shipeditor.components.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.ShipViewerControls;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class WorldPoint {
    @Getter
    private final Point2D position;

    @Getter
    private final Painter painter;

    @Getter
    private boolean cursorInBounds = false;

    @Getter @Setter
    private boolean selected = false;

    public WorldPoint(Point2D position) {
        this.position = position;
        this.painter = getPointPainter();
    }

    protected Painter getPointPainter() {
        return new Painter() {
            final Point2D point = position;
            @Override
            public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
                Paint old = g.getPaint();
                ShipViewerControls controls = PrimaryWindow.getInstance().getShipView().getControls();
                Point2D cursor = controls.getMousePoint();
                Ellipse2D dot = new Ellipse2D.Double(point.getX() - 0.25, point.getY() - 0.25, 0.5, 0.5);
                Shape result = worldToScreen.createTransformedShape(dot);

                Point2D dest = worldToScreen.transform(point, null);
                Ellipse2D outer = new Ellipse2D.Double((int) dest.getX() - 6, (int) dest.getY() - 6, 12, 12);

                cursorInBounds = outer.contains(cursor) || result.contains(cursor);
                if (selected) {
                    g.setPaint(new Color(0xBFFF0000, true));
                } else if (cursorInBounds) {
                    g.setPaint(new Color(0xBFFFFFFF, true));
                } else {
                    g.setPaint(new Color(0xBF000000, true));
                }
                g.fill(result);

                g.drawOval((int) outer.getX(), (int) outer.getY(), (int) outer.getWidth(), (int) outer.getHeight());
                g.setPaint(old);
            }
        };
    }

    public void movePosition(double x, double y) {
        this.position.setLocation(x, y);
    }

    @Override
    public String toString() {
        return "Point {" + position.getX() + "," + position.getY() + '}';
    }

}
