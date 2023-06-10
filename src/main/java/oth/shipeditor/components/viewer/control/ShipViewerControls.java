package oth.shipeditor.components.viewer.control;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.*;
import oth.shipeditor.communication.events.viewer.points.PointDragQueued;
import oth.shipeditor.communication.events.viewer.points.PointRemoveQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.components.viewer.ShipViewerPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */

@Log4j2
public final class ShipViewerControls implements ViewerControl {

    private static final double MAXIMUM_ZOOM = 1200.0;
    private static final double MINIMUM_ZOOM = 0.2;
    private final ShipViewerPanel parentViewer;

    @Getter
    private boolean rotationEnabled;

    /**
     * Previous mouse position
     */
    private final Point previousPoint = new Point();

    /**
     * Position where the mouse was previously pressed
     */
    private final Point pressPoint = new Point();

    /**
     * Used for layer dragging functionality.
     */
    private final Point layerDragPoint = new Point();

    private final int layerDragHotkey = KeyEvent.VK_SHIFT;

    @Getter
    private Point mousePoint = new Point();

    private static final double ZOOMING_SPEED = 0.15;

    private static final double ROTATION_SPEED = 0.4;

    private final BoundEditingControl boundControl;

    @Getter
    private double zoomLevel = 1;

    /**
     * @param parent Viewer which is manipulated via this instance of controls class.
     */
    private ShipViewerControls(ShipViewerPanel parent) {
        this.parentViewer = parent;
        this.rotationEnabled = false;
        this.boundControl = new BoundEditingControl();
        this.initListeners();
        this.initLayerCursorListener();
    }

    /**
     * @param parent Viewer which is manipulated via this instance of controls class.
     * @return instance of controls via factory method.
     */
    public static ShipViewerControls create(ShipViewerPanel parent) {
        return new ShipViewerControls(parent);
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerTransformsReset) {
                this.setZoomLevel(1);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationToggled checked) {
                this.rotationEnabled = checked.isSelected();
            }
        });
    }

    private void initLayerCursorListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            int keyCode = ke.getKeyCode();
            boolean isLayerDragHotkey = keyCode == layerDragHotkey;
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isLayerDragHotkey) {
                        this.parentViewer.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isLayerDragHotkey) {
                        this.parentViewer.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    break;
            }
            this.parentViewer.repaint();
            return false;
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        this.pressPoint.setLocation(point);
        // This layer dragging subroutine took a long time to figure out; careful here in the future.
        // Should any difficulties arise, employ logging liberally.
        if (this.parentViewer.getSelectedLayer() != null) {
            LayerPainter selected = this.parentViewer.getSelectedLayer();
            AffineTransform worldToScreen = this.parentViewer.getTransformWorldToScreen();
            Point2D anchor = selected.getAnchorOffset();
            // Layer anchor needs to be transformed because all mouse events are evaluated in screen coordinates.
            Point2D transformed = worldToScreen.transform(anchor, null);
            this.layerDragPoint.setLocation(e.getX() - transformed.getX(), e.getY() - transformed.getY());
        }
        AffineTransform screenToWorld = this.parentViewer.getScreenToWorld();
        this.boundControl.tryBoundCreation(e, screenToWorld);
        if (ControlPredicates.removePointPredicate.test(e)) {
            EventBus.publish(new PointRemoveQueued());
        }
        if (ControlPredicates.selectPointPredicate.test(e)) {
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
        int x = e.getX();
        int y = e.getY();
        if (ControlPredicates.translatePredicate.test(e)) {
            int dx = x - this.previousPoint.x;
            int dy = y - this.previousPoint.y;
            this.parentViewer.translate(dx, dy);
            EventBus.publish(new ViewerTransformChanged());
        }
        if (ControlPredicates.selectPointPredicate.test(e)) {
            AffineTransform screenToWorld = this.parentViewer.getScreenToWorld();
            Point2D adjustedCursor = this.getAdjustedCursor();
            EventBus.publish(new PointDragQueued(screenToWorld, adjustedCursor));
        }
        if (ControlPredicates.layerMovePredicate.test(e)) {
            int dx = x - this.layerDragPoint.x;
            int dy = y - this.layerDragPoint.y;
            AffineTransform screenToWorld = this.parentViewer.getScreenToWorld();
            LayerPainter selected = this.parentViewer.getSelectedLayer();
            Point2D snappedDifference = this.snapPointToGrid(new Point2D.Double(dx, dy), 1.0f);
            EventBus.publish(new LayerAnchorDragged(screenToWorld,
                    selected, snappedDifference));
        }
        this.previousPoint.setLocation(x, y);
        this.refreshCursorPosition(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (ControlPredicates.rotatePredicate.test(e) && this.rotationEnabled) {
            int dy = y - this.previousPoint.y;
            double toRadians = Math.toRadians(dy);
            this.parentViewer.rotate(
                    this.pressPoint.x, this.pressPoint.y,
                    toRadians * ROTATION_SPEED);
        }
        this.previousPoint.setLocation(x, y);
        this.parentViewer.repaint();
        this.refreshCursorPosition(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int wheelRotation = e.getWheelRotation();
        // Calculate the zoom factor - sign of wheel rotation argument determines the direction.
        double d = Math.pow(1 + ZOOMING_SPEED, -wheelRotation) - 1;
        double factor = 1.0 + d;
        double max = MAXIMUM_ZOOM;
        double min = MINIMUM_ZOOM;
        int x = e.getX();
        int y = e.getY();
        if (this.zoomLevel * factor >= max) {
            this.setZoomAtLimit(x, y, max);
        } else if (this.zoomLevel * factor <= min) {
            this.setZoomAtLimit(x, y, min);
        } else {
            this.parentViewer.zoom(x, y, factor, factor);
            this.setZoomLevel(this.zoomLevel * factor);
        }
        this.refreshCursorPosition(e);
    }

    private void setZoomAtLimit(int x, int y, double limit) {
        double factor = limit / zoomLevel;
        parentViewer.zoom(x, y, factor, factor);
        this.setZoomLevel(limit);
    }

    private void setZoomLevel(double level) {
        this.zoomLevel = level;
        EventBus.publish(new ViewerZoomChanged(level));
    }

    public Point2D getAdjustedCursor() {
        Point mouse = new Point(this.mousePoint);
        return this.snapPointToGrid(mouse, 2.0f);
    }

    /**
     * @param input Point that will be snapped to grid.
     * @param snappingDivisor value that will determine the size of snapping grid.
     * E.g. value of 2.0f means position snapping to 0.5 scaled pixel, while 1.0f will snap to the whole pixel.
     * @return Snapped point instance.
     */
    private Point2D snapPointToGrid(Point2D input, float snappingDivisor) {
        AffineTransform worldToScreen = parentViewer.getWorldToScreen();
        Point2D anchor = worldToScreen.transform(new Point(0, 0), null);
        // Calculate cursor position relative to anchor.
        double scale = this.zoomLevel;
        double cursorRelX = (input.getX() - anchor.getX()) / scale;
        double cursorRelY = (input.getY() - anchor.getY()) / scale;
        // Align cursor position to nearest 0.5 scaled pixel.
        double alignedCursorRelX = Math.round(cursorRelX * snappingDivisor) / snappingDivisor;
        double alignedCursorRelY = Math.round(cursorRelY * snappingDivisor) / snappingDivisor;
        // Calculate cursor position in scaled pixels.
        double cursorX = (anchor.getX() + alignedCursorRelX * scale);
        double cursorY = (anchor.getY() + alignedCursorRelY * scale);
        input.setLocation(cursorX, cursorY);
        return input;
    }

    private void refreshCursorPosition(MouseEvent event) {
        this.mousePoint = event.getPoint();
        AffineTransform screenToWorld = this.parentViewer.getScreenToWorld();
        Point2D adjusted = this.getAdjustedCursor();
        this.boundControl.setAdjustedCursor(adjusted);
        Point2D corrected = Utility.correctAdjustedCursor(adjusted, screenToWorld);
        EventBus.publish(new ViewerCursorMoved(this.mousePoint, adjusted, corrected));
    }

}
