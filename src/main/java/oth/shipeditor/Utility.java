package oth.shipeditor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class Utility {

    private Utility() {}

    public static void drawBorderedLine(Graphics2D g, Point2D first, Point2D second, Color inner) {
        Utility.drawBorderedLine(g, first, second, inner, Color.BLACK, 2f, 3f);
    }

    public static void drawBorderedLine(Graphics2D g, Point2D first, Point2D second,
                                         Color innerColor, Color outerColor, float innerWidth, float outerWidth) {
        Stroke originalStroke = g.getStroke();
        g.setColor(outerColor);
        g.setStroke(new BasicStroke(outerWidth));
        g.drawLine((int) first.getX(), (int) first.getY(), (int) second.getX(), (int) second.getY());
        g.setColor(innerColor);
        g.setStroke(new BasicStroke(innerWidth));
        g.drawLine((int) first.getX(), (int) first.getY(), (int) second.getX(), (int) second.getY());
        g.setStroke(originalStroke);
    }

    public static Point2D correctAdjustedCursor(Point2D adjustedCursor, AffineTransform screenToWorld) {
        Point2D wP = screenToWorld.transform(adjustedCursor, null);
        double roundedX = Math.round(wP.getX() * 2) / 2.0;
        double roundedY = Math.round(wP.getY() * 2) / 2.0;
        return new Point2D.Double(roundedX, roundedY);
    }

}
