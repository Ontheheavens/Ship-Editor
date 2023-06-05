package oth.shipeditor.components.viewer.painters;

import de.javagl.geom.AffineTransforms;
import de.javagl.geom.Lines;
import de.javagl.geom.Rectangles;
import de.javagl.viewer.Painter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.components.viewer.ShipViewerPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.Optional;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
@Log4j2
public final class GuidesPainter implements Painter {

    private Point2D cursor;

    private Painter guidesPaint;

    private Painter bordersPaint;

    private Painter centerPaint;

    private Painter axesPaint;

    private final AffineTransform delegateWorldToScreen;

    private final ShipViewerPanel parent;

    public GuidesPainter(ShipViewerPanel viewer) {
        this.parent = viewer;
        this.delegateWorldToScreen = new AffineTransform();
        this.cursor = new Point2D.Double(0, 0);
        this.initCursorListener();
        this.listenForToggling();
    }

    private void listenForToggling() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerGuidesToggled checked) {
                this.guidesPaint = checked.guidesEnabled() ? createGuidesPainter() : null;
                this.bordersPaint = checked.bordersEnabled() ? createBordersPainter() : null;
                this.centerPaint = checked.centerEnabled() ? createSpriteCenterPainter() : null;
                this.axesPaint = checked.axesEnabled() ? GuidesPainter.createAxesPainter() : null;
                EventBus.publish(new ViewerRepaintQueued());
            }
        });
    }

    private void initCursorListener() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                this.cursor = checked.adjusted();
            }
        });
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.delegateWorldToScreen.setTransform(worldToScreen);
        Optional.ofNullable(this.axesPaint).ifPresent(p -> p.paint(g, delegateWorldToScreen, w, h));
        Optional.ofNullable(this.guidesPaint).ifPresent(p -> p.paint(g, delegateWorldToScreen, w, h));
        Optional.ofNullable(this.bordersPaint).ifPresent(p -> p.paint(g, delegateWorldToScreen, w, h));
        Optional.ofNullable(this.centerPaint).ifPresent(p -> p.paint(g, delegateWorldToScreen, w, h));
    }

    /**
     * Draws the guides to the viewer. The guides consist of two rectangles each 1 scaled pixel wide,
     * one along the x-axis and one along the y-axis,
     * both of which intersect at the current cursor position.
     * The size and position of the rectangles are determined based
     * on the current ship sprite and zoom level.
     ** <br>
     * Note: considerable size of implementation is necessary due to the Viewer rotating functionality
     * and 0.5 scaled pixel snapping.
     */
    private Painter createGuidesPainter() {
        return (g, worldToScreen, w, h) -> {
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
            g.setPaint(old);
        };
    }

    private Painter createBordersPainter() {
        return (g, worldToScreen, w, h) -> {
            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getShipSprite() == null) return;
            RenderedImage shipSprite = layer.getShipSprite();
            int width = shipSprite.getWidth();
            int height = shipSprite.getHeight();
            Point2D layerAnchor = layer.getAnchorOffset();
            Shape spriteBorder = new Rectangle((int) layerAnchor.getX(), (int) layerAnchor.getY(), width, height);
            Shape transformed = worldToScreen.createTransformedShape(spriteBorder);
            g.draw(transformed);
            // TODO: remove later, this is for testing purposes.
            GuidesPainter.drawCrossPoint(g, worldToScreen.transform(layerAnchor, null), 4);
        };
    }

    private Painter createSpriteCenterPainter() {
        return (g, worldToScreen, w, h) -> {
            LayerPainter layer = parent.getSelectedLayer();
            if (layer == null || layer.getShipSprite() == null) return;
            RenderedImage shipSprite = layer.getShipSprite();
            Point2D anchor = layer.getAnchorOffset();
            Point spriteCenter = new Point((int) (anchor.getX() + (shipSprite.getWidth() / 2)),
                    (int) (anchor.getY() + (shipSprite.getHeight() / 2)));
            Point2D center = worldToScreen.transform(spriteCenter, null);
            // Draw the two diagonal lines centered on the sprite center.
            GuidesPainter.drawCrossPoint(g, center, 5);
        };
    }

    private static void drawCrossPoint(Graphics2D g, Point2D position, int lineSize) {
        int x = (int) position.getX(), y = (int) position.getY();
        g.drawLine(x- lineSize, y- lineSize, x+ lineSize, y+ lineSize);
        g.drawLine(x- lineSize, y+ lineSize, x+ lineSize, y- lineSize);
    }

    private static Painter createAxesPainter() {
        return (g, worldToScreen, w, h) -> {
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

}
