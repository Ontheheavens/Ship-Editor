package oth.shipeditor.components;

import de.javagl.viewer.Painter;
import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.control.ShipViewerControls;
import oth.shipeditor.components.painters.PointsPainter;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public class ShipViewerPanel extends Viewer {

    @Getter
    private boolean spriteLoaded;
    private Painter shipPaint;
    private Painter guidesPaint;
    private Painter spriteBorderPaint;
    private Painter spriteCenterPaint;
    @Getter
    private PointsPainter pointsPainter;
    @Getter
    private final ShipViewerControls controls;

    public ShipViewerPanel() {
        this.setMinimumSize(new Dimension(240, 120));
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ShipViewerPanel.this.centerViewpoint();
            }
        });
        this.setBackground(Color.GRAY);

        controls = new ShipViewerControls(this);
        this.setMouseControl(controls);
    }

    private BufferedImage getLoadedSprite() {
        return PrimaryWindow.getInstance().getShipSprite();
    }

    public void loadShipSprite(BufferedImage shipSprite) {
        this.removePainter(shipPaint);
        Painter spritePainter = (g, worldToScreen, w, h) -> {
            AffineTransform oldAT = g.getTransform();
            g.transform(worldToScreen);
            int width = shipSprite.getWidth();
            int height = shipSprite.getHeight();
            g.drawImage(shipSprite, 0, 0, width, height, null);
            g.setTransform(oldAT);
        };
        this.addPainter(spritePainter, 2);
        this.shipPaint = spritePainter;

        this.drawGuides(shipSprite);
        this.drawBorder(shipSprite);
        this.drawSpriteCenter();

        this.removePainter(pointsPainter);
        this.pointsPainter = new PointsPainter();
        this.addPainter(this.pointsPainter, 3);

        this.spriteLoaded = true;

        this.centerViewpoint();
    }

    public Point getSpriteCenter() {
        if (getLoadedSprite() != null) {
            return new Point(getLoadedSprite().getWidth() / 2, getLoadedSprite().getHeight() / 2);
        } else return new Point();
    }

    public Point getShipCenterAnchor() {
        if (getLoadedSprite() != null) {
            return new Point(0, getLoadedSprite().getHeight());
        } else return new Point();
    }

    public void centerViewpoint() {
        AffineTransform worldToScreen = this.getWorldToScreen();
        // Get the center of the sprite in screen coordinates.
        Point2D centerScreen = worldToScreen.transform(this.getSpriteCenter(), null);
        // Calculate the delta values to center the sprite.
        double dx = (this.getWidth() / 2f) - centerScreen.getX();
        double dy = (this.getHeight() / 2f) - centerScreen.getY();
        this.translate(dx, dy);
    }

    private Point2D getAdjustedCursor() {
        return controls.getAdjustedCursor();
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
    private void drawGuides(BufferedImage shipSprite) {
        this.removePainter(guidesPaint);
        Painter guidesPainter = (g, worldToScreen, w, h) -> {
            Point2D mousePoint = this.getAdjustedCursor();
            AffineTransform screenToWorld = this.getScreenToWorld();
            Point2D transformedMouse = screenToWorld.transform(mousePoint, mousePoint);
            double x = transformedMouse.getX();
            double y = transformedMouse.getY();

            double spriteW = shipSprite.getWidth();
            double spriteH = shipSprite.getHeight();
            Point2D anchor = new Point(0, 0);
            double xLeft = Math.round((anchor.getX() - 0.5) * 2) / 2.0;
            double yTop = Math.round((anchor.getY() - 0.5) * 2) / 2.0;
            double xGuide = Math.round((x - 0.5) * 2) / 2.0;
            double yGuide = Math.round((y - 0.5) * 2) / 2.0;

            Rectangle2D axisX = new Rectangle2D.Double(xLeft + 0.5, yGuide, spriteW, 1);
            Rectangle2D axisY = new Rectangle2D.Double(xGuide, yTop + 0.5, 1, spriteH);

            Paint old = g.getPaint();
            Shape guideX = worldToScreen.createTransformedShape(axisX);
            Shape guideY = worldToScreen.createTransformedShape(axisY);

            g.setPaint(new Color(0x80232323, true));
            g.draw(guideX);
            g.draw(guideY);
            g.setPaint(new Color(0x40FFFFFF, true));
            g.fill(guideX);
            g.fill(guideY);
            g.setPaint(old);
        };
        this.addPainter(guidesPainter, 5);
        this.guidesPaint = guidesPainter;
    }

    private void drawBorder(BufferedImage shipSprite) {
        this.removePainter(spriteBorderPaint);
        Painter borderPainter = (g, worldToScreen, w, h) -> {
            int width = shipSprite.getWidth();
            int height = shipSprite.getHeight();
            Rectangle worldBorder = new Rectangle(0, 0, width, height);
            Shape transformed = worldToScreen.createTransformedShape(worldBorder);
            g.draw(transformed);
        };
        this.addPainter(borderPainter, 5);
        this.spriteBorderPaint = borderPainter;
    }

    private void drawSpriteCenter() {
        this.removePainter(spriteCenterPaint);
        this.spriteCenterPaint = (g, worldToScreen, w, h) -> {
            Point2D center = worldToScreen.transform(getSpriteCenter(), null);
            // Draw the two diagonal lines centered on the sprite center.
            int x = (int) center.getX(), y = (int) center.getY(), l = 5;
            g.drawLine(x-l, y-l, x+l, y+l);
            g.drawLine(x-l, y+l, x+l, y-l);
        };
        this.addPainter(spriteCenterPaint, 5);
    }

}
