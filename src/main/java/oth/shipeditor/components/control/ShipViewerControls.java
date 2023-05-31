package oth.shipeditor.components.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.MouseControl;
import de.javagl.viewer.Predicates;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerZoomChanged;
import oth.shipeditor.communication.events.viewer.points.PointDragQueued;
import oth.shipeditor.communication.events.viewer.points.PointRemoveQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */

@Log4j2
public class ShipViewerControls implements MouseControl {

    private final ShipViewerPanel parentViewer;

    @Getter
    private boolean rotationEnabled;

    private final Predicate<MouseEvent> translatePredicate = Predicates.and(
            InputEventPredicates.buttonDown(3),
            InputEventPredicates.noModifiers()
    );

    private final Predicate<MouseEvent> selectPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.noModifiers()
    );

    private final Predicate<MouseEvent> removePointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(3),
            InputEventPredicates.shiftDown()
    );

    private final Predicate<MouseEvent> rotatePredicate = InputEventPredicates.controlDown();

    /**
     * Previous mouse position
     */
    private final Point previousPoint = new Point();

    /**
     * Position where the mouse was previously pressed
     */
    private final Point pressPoint = new Point();

    @Getter
    private Point mousePoint = new Point();

    public static final double ZOOMING_SPEED = 0.15;

    public static final double ROTATION_SPEED = 0.4;

    private final BoundEditingControl boundControl;

    @Getter
    private double zoomLevel = 1;

    public ShipViewerControls(ShipViewerPanel parent) {
        this.parentViewer = parent;
        this.setRotationEnabled(false);
        this.boundControl = new BoundEditingControl(this);
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationToggled checked) {
                setRotationEnabled(checked.isSelected());
            }
        });
    }

    private void setRotationEnabled(boolean enabled) {
        this.rotationEnabled = enabled;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent event) {
        pressPoint.setLocation(event.getPoint());
        if (!parentViewer.isSpriteLoaded()) {
            return;
        }
        this.boundControl.tryBoundCreation(event, parentViewer.getScreenToWorld());
        if (removePointPredicate.test(event)) {
            EventBus.publish(new PointRemoveQueued());
        }
        if (selectPointPredicate.test(event)) {
            EventBus.publish(new PointSelectQueued(null));
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        if (translatePredicate.test(e)) {
            int dx = e.getX() - previousPoint.x;
            int dy = e.getY() - previousPoint.y;
            parentViewer.translate(dx, dy);
        }
        if (selectPointPredicate.test(e)) {
            EventBus.publish(
                    new PointDragQueued(parentViewer.getScreenToWorld(), getAdjustedCursor()));
        }
        previousPoint.setLocation(e.getX(), e.getY());
        this.refreshCursorPosition(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (rotatePredicate.test(e) && rotationEnabled) {
            int dy = e.getY() - previousPoint.y;
            parentViewer.rotate(
                    pressPoint.x, pressPoint.y,
                    Math.toRadians(dy)* ROTATION_SPEED);
        }
        previousPoint.setLocation(e.getX(), e.getY());
        parentViewer.repaint();
        this.refreshCursorPosition(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Calculate the zoom factor - sign of wheel rotation argument determines the direction.
        double d = Math.pow(1 + ZOOMING_SPEED, -e.getWheelRotation()) - 1;
        double factor = 1.0 + d;
        double max = 1200;
        double min = 0.2;
        if (zoomLevel * factor >= max) {
            this.setZoomAtLimit(e.getX(), e.getY(), max);
        } else if (zoomLevel * factor <= min) {
            this.setZoomAtLimit(e.getX(), e.getY(), min);
        } else {
            parentViewer.zoom(e.getX(), e.getY(), factor, factor);
            this.setZoomLevel(zoomLevel * factor);
        }
    }

    private void setZoomAtLimit(int x, int y, double limit) {
        double factor = limit / zoomLevel;
        parentViewer.zoom(x, y, factor, factor);
        this.setZoomLevel(limit);
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
        SwingUtilities.invokeLater(() ->
                EventBus.publish(new ViewerZoomChanged(zoomLevel)));
    }

    public Point2D getAdjustedCursor() {
        Point2D anchor = parentViewer.getWorldToScreen().transform(new Point(0, 0), null);
        Point mouse = this.getMousePoint();
        // Calculate cursor position relative to anchor.
        double scale = this.getZoomLevel();
        double cursorRelX = (mouse.x - anchor.getX()) / scale;
        double cursorRelY = (mouse.y - anchor.getY()) / scale;
        // Align cursor position to nearest 0.5 scaled pixel.
        double alignedCursorRelX = Math.round(cursorRelX * 2) / 2f;
        double alignedCursorRelY = Math.round(cursorRelY * 2) / 2f;
        // Calculate cursor position in scaled pixels.
        double cursorX = (anchor.getX() + alignedCursorRelX * scale);
        double cursorY = (anchor.getY() + alignedCursorRelY * scale);
        return new Point2D.Double(cursorX, cursorY);
    }

    private void refreshCursorPosition(MouseEvent event) {
        mousePoint = event.getPoint();
        AffineTransform screenToWorld = parentViewer.getScreenToWorld();
        Point2D adjusted = getAdjustedCursor();
        Point2D corrected = Utility.correctAdjustedCursor(adjusted, screenToWorld);
        EventBus.publish(new ViewerCursorMoved(mousePoint, adjusted, corrected));
    }

}
