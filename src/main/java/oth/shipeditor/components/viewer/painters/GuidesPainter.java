package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.ShipViewerPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Optional;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
@Log4j2
public final class GuidesPainter implements Painter {

    // TODO: This is currently a mess with tangled-up logic; needs to be sorted out soon.

    private Point2D cursor;

    private Painter guidesPaint;

    private Painter bordersPaint;

    private Painter centerPaint;

    private final AffineTransform delegateWorldToScreen;

    private final ShipViewerPanel parent;

    public GuidesPainter(ShipViewerPanel viewer) {
        this.parent = viewer;
        this.delegateWorldToScreen = new AffineTransform();
        this.cursor = new Point2D.Double(0, 0);
        this.initCursorListener();
        this.listenForToggling();
    }

    // TODO: Implement guides switch for layer selection. Remember that checked.selected() can be null now.

    private void listenForLayerChange() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {

            }
        });
    }

    private void listenForToggling() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerGuidesToggled checked) {
                LayerPainter selectedLayer = parent.getSelectedLayer();
                if (selectedLayer == null) return;
                BufferedImage shipSprite = selectedLayer.getShipSprite();
                this.guidesPaint = checked.guidesEnabled() ? createGuidesPainter(shipSprite) : null;
                this.bordersPaint = checked.bordersEnabled() ? GuidesPainter.createBordersPainter(shipSprite) : null;
                this.centerPaint = checked.centerEnabled() ? GuidesPainter.createSpriteCenterPainter(shipSprite) : null;
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
        Optional.ofNullable(this.guidesPaint).ifPresent(p -> p.paint(g, this.delegateWorldToScreen, w, h));
        Optional.ofNullable(this.bordersPaint).ifPresent(p -> p.paint(g, worldToScreen, w, h));
        Optional.ofNullable(this.centerPaint).ifPresent(p -> p.paint(g, worldToScreen, w, h));
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
    private Painter createGuidesPainter(RenderedImage shipSprite) {
        return (g, worldToScreen, w, h) -> {
            Point2D mousePoint = this.cursor;
            AffineTransform screenToWorld = this.parent.getScreenToWorld();
            Point2D transformedMouse = screenToWorld.transform(mousePoint, null);
            double x = transformedMouse.getX();
            double y = transformedMouse.getY();

            double spriteW = shipSprite.getWidth();
            double spriteH = shipSprite.getHeight();
            Point2D anchor = new Point(0, 0);
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

    private static Painter createBordersPainter(RenderedImage shipSprite) {
        return (g, worldToScreen, w, h) -> {
            int width = shipSprite.getWidth();
            int height = shipSprite.getHeight();
            Shape worldBorder = new Rectangle(0, 0, width, height);
            Shape transformed = worldToScreen.createTransformedShape(worldBorder);
            g.draw(transformed);
        };
    }

    private static Painter createSpriteCenterPainter(RenderedImage shipSprite) {
        return (g, worldToScreen, w, h) -> {
            Point spriteCenter = new Point(shipSprite.getWidth() / 2, shipSprite.getHeight() / 2);
            Point2D center = worldToScreen.transform(spriteCenter, null);
            // Draw the two diagonal lines centered on the sprite center.
            int x = (int) center.getX(), y = (int) center.getY(), i = 5;
            g.drawLine(x-i, y-i, x+i, y+i);
            g.drawLine(x-i, y+i, x+i, y-i);
        };
    }

}
