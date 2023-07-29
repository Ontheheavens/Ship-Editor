package oth.shipeditor.utility.graphics;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
@Log4j2
public final class DrawUtilities {

    private DrawUtilities() {
    }

    public static void drawScreenLine(Graphics2D g, Point2D start, Point2D finish,
                                         Color color, float thickness) {
        Stroke originalStroke = g.getStroke();
        Color originalColor = g.getColor();

        g.setColor(color);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());

        g.setStroke(originalStroke);
        g.setColor(originalColor);
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

    public static void drawOutlined(Graphics2D g, Shape shape, Paint color) {
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(5));
        g.setPaint(Color.BLACK);
        g.draw(shape);
        g.setStroke(new BasicStroke(3));
        g.setPaint(color);
        g.draw(shape);
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
     * Note: this method is for painting in screen coordinates only.
     * @param screenPoint desired position of painted String.
     * @param fontInput if null, default value is Orbitron 14.
     * @param strokeInput if null, default value is 2.5f with rounded caps and joins.
     * @param corner determines what corner of painted text's bounding box will correspond to passed screen position.
     * E.g. if BOTTOM_RIGHT, the label will be painted to the upper left of screen point.
     * @return resulting {@link Shape} instance of bounds of the drawn text, from which bounding box positions can be retrieved.
     */

    @SuppressWarnings("MethodWithTooManyParameters")
    public static Shape paintScreenTextOutlined(Graphics2D g, String text, Font fontInput, Stroke strokeInput,
                                                Point2D screenPoint, RectangleCorner corner) {
        Font font = fontInput;
        if (font == null) {
            font = Utility.getOrbitron(14);
        }

        GlyphVector glyphVector = font.createGlyphVector(g.getFontRenderContext(), text);
        Shape textShape = glyphVector.getOutline();

        Rectangle2D bounds = textShape.getBounds2D();
        bounds.setRect(screenPoint.getX(), screenPoint.getY(), bounds.getWidth(), bounds.getHeight());
        Point2D delta = ShapeUtilities.calculateCornerCoordinates(bounds, corner);
        double x = delta.getX();
        double y = delta.getY();

        Shape textShapeTranslated = ShapeUtilities.translateShape(textShape,x, y);
        Shape translatedBounds = ShapeUtilities.translateShape(glyphVector.getLogicalBounds(),x, y);

        DrawUtilities.paintOutlinedText(g, translatedBounds, textShapeTranslated, strokeInput);

        return ShapeUtilities.translateShape(glyphVector.getVisualBounds(),x, y);
    }

    /**
     * @param bounds will be used to draw shaded background of text.
     * @param strokeInput if null, default BasicStroke of 2.5 will be used, with round caps and joins.
     */
    public static void paintOutlinedText(Graphics2D g, Shape bounds, Shape textShapeTransformed, Stroke strokeInput) {
        Color outlineColor = Color.BLACK;
        Color fillColor = Color.WHITE;

        Stroke stroke = strokeInput;
        if (stroke == null) {
            stroke = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }

        RenderingHints originalHints = g.getRenderingHints();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        DrawUtilities.fillShape(g, bounds, ColorUtilities.setColorAlpha(outlineColor, 50));
        DrawUtilities.outlineShape(g, textShapeTransformed, outlineColor, stroke);
        DrawUtilities.fillShape(g,textShapeTransformed, fillColor);

        g.setRenderingHints(originalHints);
    }

    /**
     * Draws the specified graphics action with conditional opacity based on the zoom level.
     * The opacity of the graphics action is adjusted according to the zoom level,
     * so that it is fully transparent (invisible) when the zoom level is 20 or below,
     * and gradually becomes more opaque as the zoom level increases above 20 until it reaches
     * fully opaque (alpha 1.0) when the zoom level exceeds 40.
     *
     * @param action The GraphicsAction object representing the graphics action to be drawn.
     *               The drawing behavior should not rely on the current alpha composite settings,
     *               as they will be temporarily adjusted within this method.
     */
    public static void drawWithConditionalOpacity(Graphics2D g, GraphicsAction action) {
        double zoomLevel = StaticController.getZoomLevel();

        double alpha;
        if (zoomLevel > 20) {
            alpha = (zoomLevel - 20.0) / 20.0;
            alpha = Math.min(alpha, 1.0);
        } else return;

        Composite old = Utility.setAlphaComposite(g, alpha);

        action.draw(g);

        g.setComposite(old);
    }

}