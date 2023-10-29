package oth.shipeditor.components.viewer.painters;

import de.javagl.geom.AffineTransforms;
import de.javagl.geom.Lines;
import de.javagl.geom.Rectangles;
import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.BoundPointsPainter;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.DrawingParameters;
import oth.shipeditor.utility.graphics.RectangleCorner;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
@Log4j2
public final class GuidesPainters {

    @Getter
    private final Painter guidesPaint;
    @Getter
    private final Painter bordersPaint;
    @Getter
    private final Painter centerPaint;
    @Getter
    private final Painter axesPaint;

    private boolean drawGuides;
    private boolean drawBorders;
    private boolean drawCenter;
    private boolean drawAxes;

    private final PrimaryViewer parent;

    public GuidesPainters(PrimaryViewer viewer) {
        this.parent = viewer;
        this.listenForToggling();

        this.guidesPaint = createGuidesPainter();
        this.bordersPaint = createBordersPainter();
        this.centerPaint = createSpriteCenterPainter();
        this.axesPaint = createAxesPainter();
    }

    private void listenForToggling() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerGuidesToggled checked) {
                this.drawGuides = checked.guidesEnabled();
                this.drawBorders = checked.bordersEnabled();
                this.drawCenter = checked.centerEnabled();
                this.drawAxes = checked.axesEnabled();
                EventBus.publish(new ViewerRepaintQueued());
            }
        });
    }

    /**
     * Draws the guides to the viewer. The guides consist of two rectangles each 1 scaled pixel wide,
     * one along the x-axis and one along the y-axis,
     * both of which intersect at the current cursor position.
     * The size and position of the rectangles are determined based
     * on the current ship sprite and zoom level.
     */
    private Painter createGuidesPainter() {
        return (g, worldToScreen, w, h) -> {
            if (!drawGuides) return;
            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getSprite() == null) return;
            RenderedImage shipSprite = layer.getSpriteImage();

            Point2D adjustedCursor = StaticController.getAdjustedCursor();

            AffineTransform screenToWorld = layer.getWithRotationInverse(parent.getWorldToScreen());

            Point2D transformedMouse = screenToWorld.transform(adjustedCursor, null);
            double x = transformedMouse.getX();
            double y = transformedMouse.getY();

            double spriteW = shipSprite.getWidth();
            double spriteH = shipSprite.getHeight();

            Point2D anchor = layer.getAnchor();

            double xLeft = anchor.getX() - 0.5;
            double yTop = anchor.getY() - 0.5;

            Point2D transformedRaw = screenToWorld.transform(StaticController.getRawCursor(), null);
            double xGuide = transformedRaw.getX() - 0.5;
            double yGuide = transformedRaw.getY() - 0.5;
            if (ControlPredicates.isCursorSnappingEnabled()) {
                xLeft = Math.round(xLeft * 2) / 2.0;
                yTop = Math.round(yTop * 2) / 2.0;
                xGuide = Math.round((x - 0.5) * 2) / 2.0;
                yGuide = Math.round((y - 0.5) * 2) / 2.0;
            }

            Point2D crossCenter = new Point2D.Double(xGuide + 0.5, yGuide + 0.5);

            Shape axisX = new Rectangle2D.Double(xLeft + 0.5, yGuide, spriteW, 1);
            Shape axisY = new Rectangle2D.Double(xGuide, yTop + 0.5, 1, spriteH);

            Paint old = g.getPaint();
            Shape guideX = worldToScreen.createTransformedShape(axisX);
            Shape guideY = worldToScreen.createTransformedShape(axisY);

            g.setPaint(new Color(0x80232323, true));
            g.draw(guideX);
            g.draw(guideY);
            g.setPaint(new Color(0x40FFFFFF, true));
            g.fill(guideX);
            g.fill(guideY);

            Shape targetSquare = new Rectangle2D.Double(xGuide, yGuide, 1, 1);
            Shape transformed = worldToScreen.createTransformedShape(targetSquare);

            DrawUtilities.outlineShape(g, transformed, Color.BLACK, 3);
            DrawUtilities.outlineShape(g, transformed, Color.WHITE, 1);

            double crossSize = 0.15;

            Shape crossLineX = new Line2D.Double(crossCenter.getX() - crossSize, crossCenter.getY(),
                    crossCenter.getX() + crossSize, crossCenter.getY());
            Shape crossLineY = new Line2D.Double(crossCenter.getX(), crossCenter.getY() - crossSize,
                    crossCenter.getX(), crossCenter.getY() + crossSize);

            Shape transformedCrossX = worldToScreen.createTransformedShape(crossLineX);
            Shape transformedCrossY = worldToScreen.createTransformedShape(crossLineY);

            g.setPaint(Color.BLACK);

            g.draw(transformedCrossX);
            g.draw(transformedCrossY);
            g.setPaint(old);

            GuidesPainters.drawPointPositionHint(g, StaticController.getRawCursor(), layer);
        };
    }

    private Painter createBordersPainter() {
        return new BordersPainter();
    }

    private Painter createSpriteCenterPainter() {
        return new SpriteCenterPainter();
    }

    private Painter createAxesPainter() {
        return (g, worldToScreen, w, h) -> {
            if (!drawAxes) return;
            AffineTransform worldToScreenCopy = new AffineTransform(worldToScreen);

            Rectangle2D worldBounds = new Rectangle2D.Double();
            Rectangle2D screenBounds = new Rectangle2D.Double(0, 0, w, h);
            AffineTransform screenToWorld = AffineTransforms.invert(worldToScreenCopy, null);
            Rectangles.computeBounds(screenToWorld, screenBounds, worldBounds);

            Paint old = g.getPaint();
            g.setPaint(Color.DARK_GRAY);

            Line2D.Double axisLineX = new Line2D.Double(worldBounds.getMinX(), 0, worldBounds.getMaxX(), 0);
            Line2D.Double transformedX = new Line2D.Double();
            Lines.transform(worldToScreenCopy, axisLineX, transformedX);
            g.draw(transformedX);

            Line2D.Double axisLineY = new Line2D.Double(0, worldBounds.getMinY(), 0, worldBounds.getMaxY());
            Line2D.Double transformedY = new Line2D.Double();
            Lines.transform(worldToScreenCopy, axisLineY, transformedY);
            g.draw(transformedY);

            g.setPaint(old);
        };
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private static void drawPointPositionHint(Graphics2D g, Point2D position, LayerPainter painter) {
        if (StaticController.getEditorMode() == EditorInstrument.BOUNDS) {
            Font hintFont = Utility.getOrbitron(12);
            if (!(painter instanceof ShipPainter checkedPainter)) return;
            BoundPointsPainter boundsPainter = checkedPainter.getBoundsPainter();
            if (boundsPainter == null) return;
            WorldPoint selected = boundsPainter.getSelected();
            if (selected == null) return;
            Point2D boundPosition = selected.getCoordinatesForDisplay();

            String toDraw = boundPosition.getX() + ", " + boundPosition.getY();
            double x = position.getX(), y = position.getY();

            Point2D.Double screenPosition = new Point2D.Double(x + 20, y + 14);

            DrawUtilities.paintScreenTextOutlined(g, toDraw, hintFont, null,
                    screenPosition, RectangleCorner.TOP_LEFT);
        }
    }

    private class SpriteCenterPainter implements Painter {

        final TextPainter label = new TextPainter();

        @Override
        public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
            if (!drawCenter) return;
            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getSprite() == null) return;
            Point2D spriteCenter = layer.getSpriteCenter();
            WorldPoint pointInput = new BaseWorldPoint(spriteCenter);
            Point2D toDisplay = pointInput.getCoordinatesForDisplay();

            // Draw two lines centered on the sprite center.
            SpriteCenterPainter.drawSpriteCenter(g, worldToScreen, spriteCenter);
            String spriteCenterCoords = "Sprite Center (" + toDisplay.getX() + ", " + toDisplay.getY() + ")";

            DrawUtilities.drawWithConditionalOpacity(g, graphics2D -> {
                label.setWorldPosition(spriteCenter);
                label.setText(spriteCenterCoords);
                label.paintText(graphics2D, worldToScreen);
            });
        }

        private static void drawSpriteCenter(Graphics2D g, AffineTransform worldToScreen,
                                             Point2D positionWorld) {
            double worldSize = 0.5;
            double thickness = 0.05;
            double screenSize = 12;
            DrawingParameters parameters = DrawingParameters.builder()
                    .withWorldSize(worldSize)
                    .withWorldThickness(thickness)
                    .withScreenSize(screenSize)
                    .withPaintColor(Color.BLACK)
                    .build();
            DrawUtilities.drawDynamicCross(g, worldToScreen, positionWorld, parameters);
        }

    }

    private class BordersPainter implements Painter {

        @Override
        public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
            if (!drawBorders) return;

            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getSprite() == null) return;
            RenderedImage shipSprite = layer.getSpriteImage();
            int width = shipSprite.getWidth();
            int height = shipSprite.getHeight();
            Point2D layerAnchor = layer.getAnchor();
            Shape spriteBorder = new Rectangle((int) layerAnchor.getX(), (int) layerAnchor.getY(), width, height);
            Shape transformed = worldToScreen.createTransformedShape(spriteBorder);

            Stroke oldStroke = g.getStroke();
            Paint oldPaint = g.getPaint();

            g.setStroke(new BasicStroke(3));
            g.draw(transformed);
            g.setStroke(oldStroke);
            g.setPaint(Color.WHITE);
            g.draw(transformed);

            g.setPaint(oldPaint);
        }

    }

}
