package oth.shipeditor.components.viewer.painters;

import de.javagl.geom.AffineTransforms;
import de.javagl.geom.Lines;
import de.javagl.geom.Rectangles;
import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.PrimaryShipViewer;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.utility.ApplicationDefaults;
import oth.shipeditor.utility.Utility;

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
@SuppressWarnings("ClassWithTooManyFields")
@Log4j2
public final class GuidesPainters {

    private Point2D cursor;

    private Point2D rawCursor;

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

    private final PrimaryShipViewer parent;

    public GuidesPainters(PrimaryShipViewer viewer) {
        this.parent = viewer;
        this.cursor = new Point2D.Double(0, 0);
        this.initCursorListener();
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

    private void initCursorListener() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                this.cursor = checked.adjusted();
                this.rawCursor = checked.rawCursor();
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
            if (layer == null || layer.getShipSprite() == null) return;
            RenderedImage shipSprite = layer.getShipSprite();

            Point2D mousePoint = this.cursor;
            AffineTransform screenToWorld = this.parent.getScreenToWorld();
            Point2D transformedMouse = screenToWorld.transform(mousePoint, null);
            double x = transformedMouse.getX();
            double y = transformedMouse.getY();

            double spriteW = shipSprite.getWidth();
            double spriteH = shipSprite.getHeight();
            Point2D anchor = layer.getAnchorOffset();
            double xLeft = Math.round((anchor.getX() - 0.5) * 2) / 2.0;
            double yTop = Math.round((anchor.getY() - 0.5) * 2) / 2.0;
            double xGuide = Math.round((x - 0.5) * 2) / 2.0;
            double yGuide = Math.round((y - 0.5) * 2) / 2.0;

            Shape axisX = new Rectangle2D.Double(xLeft + 0.5, yGuide, spriteW, 1);
            Shape axisY = new Rectangle2D.Double(xGuide, yTop + 0.5, 1, spriteH);

            Paint old = g.getPaint();
            Shape guideX = worldToScreen.createTransformedShape(axisX);
            Shape guideY = worldToScreen.createTransformedShape(axisY);

            g.setPaint(new Color(0x80232323, true));
            g.draw(guideX); g.draw(guideY);
            g.setPaint(new Color(0x40FFFFFF, true));
            g.fill(guideX); g.fill(guideY);


            Shape targetSquare = new Rectangle2D.Double(xGuide, yGuide, 1, 1);
            Shape transformed = worldToScreen.createTransformedShape(targetSquare);

            g.setPaint(Color.BLACK);

            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(3));
            g.draw(transformed);
            g.setStroke(oldStroke);
            g.setPaint(Color.WHITE);
            g.draw(transformed);
            g.setPaint(old);

            Point2D crossCenter = new Point2D.Double(xGuide + 0.5, yGuide + 0.5);
            double crossSize = 0.15;

            Shape crossLineX = new Line2D.Double(crossCenter.getX() - crossSize, crossCenter.getY(),
                    crossCenter.getX() + crossSize, crossCenter.getY());
            Shape crossLineY = new Line2D.Double(crossCenter.getX(), crossCenter.getY() - crossSize,
                    crossCenter.getX(), crossCenter.getY() + crossSize);

            Shape transformedCrossX = worldToScreen.createTransformedShape(crossLineX);
            Shape transformedCrossY = worldToScreen.createTransformedShape(crossLineY);

            g.draw(transformedCrossX);
            g.draw(transformedCrossY);
            g.setPaint(old);

            GuidesPainters.drawPointPositionHint(g, this.rawCursor, layer);
        };
    }

    private Painter createBordersPainter() {
        return (g, worldToScreen, w, h) -> {
            if (!drawBorders) return;

            Paint old = g.getPaint();
            g.setPaint(Color.BLACK);

            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getShipSprite() == null) return;
            RenderedImage shipSprite = layer.getShipSprite();
            int width = shipSprite.getWidth();
            int height = shipSprite.getHeight();
            Point2D layerAnchor = layer.getAnchorOffset();
            Shape spriteBorder = new Rectangle((int) layerAnchor.getX(), (int) layerAnchor.getY(), width, height);
            Shape transformed = worldToScreen.createTransformedShape(spriteBorder);

            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(3));
            g.draw(transformed);
            g.setStroke(oldStroke);
            g.setPaint(Color.WHITE);

            g.draw(transformed);

            g.setPaint(old);
        };
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

    private static void drawPointPositionHint(Graphics2D g, Point2D position, LayerPainter painter) {
        if (InstrumentTabsPane.getCurrentMode() == InstrumentMode.BOUNDS) {
            Font hintFont = Utility.getOrbitron(12);
            g.setFont(hintFont);

            Paint old = g.getPaint();

            g.setPaint(ApplicationDefaults.VIEWER_FONT_COLOR);
            BoundPointsPainter boundsPainter = painter.getBoundsPainter();
            if (boundsPainter == null) return;
            WorldPoint selected = boundsPainter.getSelected();
            if (selected == null) return;
            Point2D boundPosition = selected.getCoordinatesForDisplay();

            String toDraw = (int) Math.round(boundPosition.getX()) + "," + (int) Math.round(boundPosition.getY());
            int x = (int) position.getX(), y = (int) position.getY();

            g.drawString(toDraw, x + 10, y - 10);

            g.setPaint(old);
        }
    }

    private class SpriteCenterPainter implements Painter {

        final LabelPainter label = SpriteCenterPainter.createCenterLabelPainter();

        private static LabelPainter createCenterLabelPainter() {
            LabelPainter painter = new LabelPainter();
            Font font = Utility.getOrbitron(16).deriveFont(0.25f);
            painter.setFont(font);
            painter.setLabelAnchor(-0.115f, 0.55f);
            return painter;
        }

        @Override
        public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
            if (!drawCenter) return;
            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getShipSprite() == null) return;
            RenderedImage shipSprite = layer.getShipSprite();
            Point2D anchor = layer.getAnchorOffset();
            Point spriteCenter = new Point((int) (anchor.getX() + (shipSprite.getWidth() / 2)),
                    (int) (anchor.getY() + (shipSprite.getHeight() / 2)));
            WorldPoint pointInput = new BaseWorldPoint(spriteCenter);
            Point2D toDisplay = pointInput.getCoordinatesForDisplay();
            label.setLabelLocation(spriteCenter.getX() + 0.25, spriteCenter.getY());
            // Draw two lines centered on the sprite center.
            SpriteCenterPainter.drawSpriteCenter(g, worldToScreen, spriteCenter);
            String spriteCenterCoords = "Sprite Center (" + toDisplay.getX() + ", " + toDisplay.getY() + ")";

            Paint old = label.getPaint();
            label.setPaint(ApplicationDefaults.VIEWER_FONT_COLOR);
            label.paint(g, worldToScreen, w, h, spriteCenterCoords);
            label.setPaint(old);
        }

        private static void drawSpriteCenter(Graphics2D g, AffineTransform worldToScreen,
                                             Point2D positionWorld) {
            double size = 0.5;
            double thickness = 0.05;

            double x = positionWorld.getX(), y = positionWorld.getY();

            double horizontalX = x - size / 2;
            double horizontalY = y - thickness / 2;

            double verticalX = x - thickness / 2;
            double verticalY = y - size / 2;

            Shape horizontal = new Rectangle2D.Double(horizontalX, horizontalY, size, thickness);
            Shape vertical = new Rectangle2D.Double(verticalX, verticalY, thickness, size);

            Shape transformedH = worldToScreen.createTransformedShape(horizontal);
            Shape transformedV = worldToScreen.createTransformedShape(vertical);

            double screenSize = 4;

            Point2D positionScreen = worldToScreen.transform(positionWorld, null);

            double screenX = positionScreen.getX(), screenY = positionScreen.getY();

            // Extracting the rotation component from the worldToScreen transform.
            // I don't understand half of it - bless my new machine overlords.
            double[] matrix = new double[6];
            worldToScreen.getMatrix(matrix);
            double scaleX = Math.sqrt(matrix[0] * matrix[0] + matrix[1] * matrix[1]);
            double rotationAngle = -Math.atan2(matrix[1] / scaleX, matrix[0] / scaleX);

            AffineTransform rotationTransform = new AffineTransform();
            // New AffineTransform by default is focused  on the 0,0 in screen coordinates.
            // We have to center it on our point, do the rotation, then translate back.
            rotationTransform.translate(screenX, screenY);
            rotationTransform.rotate(-rotationAngle);
            rotationTransform.translate(-screenX, -screenY);

            Shape crossLineX = new Line2D.Double(screenX - screenSize, screenY,
                    screenX + screenSize, screenY);
            Shape crossLineY = new Line2D.Double(screenX, screenY - screenSize,
                    screenX, screenY + screenSize);

            Shape transformedX = rotationTransform.createTransformedShape(crossLineX);
            Shape transformedY = rotationTransform.createTransformedShape(crossLineY);

            Paint old = g.getPaint();

            g.setPaint(Color.BLACK);
            g.fill(transformedH);
            g.fill(transformedV);

            g.draw(transformedX);
            g.draw(transformedY);

            g.setPaint(old);
        }

    }

}
