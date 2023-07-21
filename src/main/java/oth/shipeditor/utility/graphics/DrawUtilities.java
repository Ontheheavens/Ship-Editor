package oth.shipeditor.utility.graphics;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.utility.RectangleCorner;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
@SuppressWarnings("DuplicatedCode")
@Log4j2
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

    public static void outlineShape(Graphics2D g, Shape shape, Paint color, double strokeWidth) {
        DrawUtilities.outlineShape(g, shape, color, new BasicStroke((float) strokeWidth));
    }

    @SuppressWarnings("WeakerAccess")
    public static void outlineShape(Graphics2D g, Shape shape, Paint color, Stroke stroke) {
        Object renderingHint = g.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Paint old = g.getPaint();
        Stroke oldStroke = g.getStroke();
        g.setStroke(stroke);
        g.setPaint(color);

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

    public static void drawCentroid(Graphics2D g, Shape centroid, Paint color) {
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

    public static Shape paintScreenTextOutlined(Graphics2D g, String text, Point2D screenPosition) {
        return DrawUtilities.paintScreenTextOutlined(g, text, null, screenPosition);
    }

    @SuppressWarnings("WeakerAccess")
    public static Shape paintScreenTextOutlined(Graphics2D g, String text, Font hintFont, Point2D screenPosition) {
        return DrawUtilities.paintScreenTextOutlined(g, text, hintFont,
                null, screenPosition, RectangleCorner.BOTTOM_RIGHT);
    }

    /**
     * @param screenPoint desired position of painted String.
     * @param fontInput if null, default value is Orbitron 14.
     * @param strokeInput if null, default value is 2.5f with rounded caps and joins.
     * @param corner determines what corner of painted text's bounding box will correspond to passed screen position.
     * E.g. if BOTTOM_RIGHT, the label will be painted to the upper left of screen point.
     * @return resulting {@link Shape} instance of the drawn text, from which bounding box positions can be retrieved.
     */
    @SuppressWarnings({"WeakerAccess", "MethodWithTooManyParameters"})
    public static Shape paintScreenTextOutlined(Graphics2D g, String text, Font fontInput, Stroke strokeInput,
                                                Point2D screenPoint, RectangleCorner corner) {
        Font font = fontInput;
        if (font == null) {
            font = Utility.getOrbitron(14);
        }

        Stroke stroke = strokeInput;
        if (stroke == null) {
            stroke = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }

        Color outlineColor = Color.BLACK;
        Color fillColor = Color.WHITE;

        RenderingHints originalHints = g.getRenderingHints();
        Shape textShape = ShapeUtilities.getTextShape(g, text, font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Rectangle2D bounds = textShape.getBounds2D();
        bounds.setRect(screenPoint.getX(), screenPoint.getY(), bounds.getWidth(), bounds.getHeight());
        Point2D delta = ShapeUtilities.calculateCornerCoordinates(bounds, corner);
        double x = delta.getX();
        double y = delta.getY();
        Shape textShapeTranslated = ShapeUtilities.translateShape(textShape,x, y);

        DrawUtilities.fillShape(g, textShapeTranslated.getBounds2D(),
                ColorUtilities.setHalfAlpha(outlineColor));
        DrawUtilities.outlineShape(g, textShapeTranslated, outlineColor, stroke);
        DrawUtilities.fillShape(g,textShapeTranslated, fillColor);

        g.setRenderingHints(originalHints);

        return textShapeTranslated;
    }

}
