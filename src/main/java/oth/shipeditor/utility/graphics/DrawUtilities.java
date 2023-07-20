package oth.shipeditor.utility.graphics;

import oth.shipeditor.components.viewer.entities.BaseWorldPoint;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
@SuppressWarnings("DuplicatedCode")
public final class DrawUtilities {

    private DrawUtilities() {
    }

    public static void drawScreenLine(Graphics2D canvas, Point2D start, Point2D finish,
                                         Color color, float thickness) {
        Stroke originalStroke = canvas.getStroke();
        Color originalColor = canvas.getColor();
        canvas.setColor(color);
        canvas.setStroke(new BasicStroke(thickness));
        canvas.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());
        canvas.setStroke(originalStroke);
        canvas.setColor(originalColor);
    }

    public static void drawDynamicCross(Graphics2D g,
                                        AffineTransform worldToScreen,
                                        Point2D positionWorld,
                                        DrawingParameters miscParameters) {
        double worldSize = miscParameters.getWorldSize();
        double minScreenSize = miscParameters.getScreenSize();

        double worldThickness = miscParameters.getWorldThickness();

        Shape cross = ShapeUtilities.createPerpendicularThickCross(positionWorld, worldSize, worldThickness);
        Shape transformed = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                positionWorld, cross, minScreenSize);

        Color color = miscParameters.getPaintColor();
        DrawUtilities.fillShape(g, transformed,color);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static void outlineShape(Graphics2D g, Shape shape,
                                             Paint outline, float outlineWidth) {
        Object renderingHint = g.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Paint old = g.getPaint();
        Stroke oldStroke = g.getStroke();

        g.setStroke(new BasicStroke(outlineWidth));
        g.setPaint(outline);

        g.draw(shape);

        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, renderingHint);
        g.setStroke(oldStroke);
        g.setPaint(old);
    }

    @SuppressWarnings("WeakerAccess")
    public static void fillShape(Graphics2D g, Shape shape, Paint fill) {
        Paint old = g.getPaint();

        g.setPaint(fill);

        g.fill(shape);

        g.setPaint(old);
    }

    public static void drawCentroid(Graphics2D g, Shape centroid, Color color) {
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(5));
        g.setPaint(Color.BLACK);
        g.draw(centroid);
        g.setStroke(new BasicStroke(3));
        g.setPaint(color);
        g.draw(centroid);
        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
    }

}
