package oth.shipeditor.components.viewer.control;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.DeleteButtonPressed;
import oth.shipeditor.communication.events.viewer.control.*;
import oth.shipeditor.communication.events.viewer.layers.LayerRotationQueued;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.points.ship.EngineSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Note 19.09.2023: This class has some serious conceptual issues: ideally, it should not be concerned with entities,
 * should not publish a plethora of different events that are opinionated as to what their receivers should do.
 * Instead, its sole purpose should be collecting input control data and publishing it on event bus;
 * Interested classes like painters and viewer entities should listen for that input.
 * However, given the constraints and the fact that current implementation works fine, best to leave things be.
 * @author Ontheheavens
 * @since 29.04.2023
 */

@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class LayerViewerControls implements ViewerControl {

    private final PrimaryViewer parentViewer;

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

    private static final int layerDragHotkey = KeyEvent.VK_SHIFT;

    @Getter
    private Point mousePoint = new Point();

    @Getter
    private double zoomLevel = 1;

    @Getter
    private double rotationDegree;

    /**
     * @param parent Viewer which is manipulated via this instance of controls class.
     */
    private LayerViewerControls(PrimaryViewer parent) {
        this.parentViewer = parent;
        this.rotationEnabled = true;
        this.initListeners();
        this.initLayerCursorListener();
        this.initKeyBinding();
    }

    private void initKeyBinding() {
        InputMap inputMap = parentViewer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        String deleteKey = "Delete";
        inputMap.put(KeyStroke.getKeyStroke((char)KeyEvent.VK_DELETE), deleteKey);
        ActionMap actionMap = parentViewer.getActionMap();
        actionMap.put(deleteKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventBus.publish(new PointRemoveQueued(null, false));
                EventBus.publish(new DeleteButtonPressed());
            }
        });
    }

    /**
     * @param parent Viewer which is manipulated via this instance of controls class.
     * @return instance of controls via factory method.
     */
    public static LayerViewerControls create(PrimaryViewer parent) {
        return new LayerViewerControls(parent);
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerTransformsReset) {
                this.setZoomLevel(1);
                this.rotationDegree = 0;
                StaticController.setRotationRadians(0);
                EventBus.publish(new ViewerTransformRotated());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationToggled checked) {
                this.rotationEnabled = checked.isSelected();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerZoomSet checked) {
                this.setZoomExact(checked.level());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationSet checked) {
                this.rotateExact(checked.degrees());
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
                        this.parentViewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isLayerDragHotkey) {
                        this.parentViewer.setCursor(Cursor.getDefaultCursor());
                    }
                    break;
            }
            this.parentViewer.setRepaintQueued();
            return false;
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        this.pressPoint.setLocation(point);
        // This layer dragging feature took a long time to figure out; careful here in the future.
        // Should any difficulties arise, employ logging liberally.
        if (this.parentViewer.getSelectedLayer() != null) {
            LayerPainter selected = this.parentViewer.getSelectedLayer();
            AffineTransform worldToScreen = this.parentViewer.getTransformWorldToScreen();
            Point2D anchor = selected.getAnchor();
            // Layer anchor needs to be transformed because all mouse events are evaluated in screen coordinates.
            Point2D transformed = worldToScreen.transform(anchor, null);
            this.layerDragPoint.setLocation(e.getX() - transformed.getX(), e.getY() - transformed.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.publishMousePressWithPosition(e, point);
        }
        if (ControlPredicates.removePointPredicate.test(e)) {
            EventBus.publish(new PointRemoveQueued(null, false));
        }
        if (!ControlPredicates.selectPointPredicate.test(e)) return;
        if (ControlPredicates.getSelectionMode() == PointSelectionMode.STRICT) {
            EventBus.publish(new PointSelectQueued(null));
        }
    }

    /**
     * Respective hotkey checks are being done in points painter itself.
     */
    private void publishMousePressWithPosition(MouseEvent event, Point2D point) {
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D position = screenToWorld.transform(point, null);
        if (ControlPredicates.isCursorSnappingEnabled()) {
            Point2D screenPoint = this.getAdjustedCursor();
            position = Utility.correctAdjustedCursor(screenPoint, screenToWorld);
        }
        EventBus.publish(new PointCreationQueued(position));
        if (event.isControlDown()) {
            EventBus.publish(new FeatureInstallQueued(position));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        EventBus.publish(new ViewerMouseReleased());
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @SuppressWarnings("IfStatementWithTooManyBranches")
    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        LayerPainter selected = this.parentViewer.getSelectedLayer();
        AffineTransform screenToWorld = this.parentViewer.getScreenToWorld();
        AffineTransform rotatedTransform = StaticController.getScreenToWorld();
        if (ControlPredicates.translatePredicate.test(e)) {
            int dx = x - this.previousPoint.x;
            int dy = y - this.previousPoint.y;
            this.parentViewer.translate(dx, dy);
            EventBus.publish(new ViewerTransformChanged());
        } else if (ControlPredicates.layerMovePredicate.test(e)) {
            int dx = x - this.layerDragPoint.x;
            int dy = y - this.layerDragPoint.y;
            if (selected != null) {
                Point2D snappedDifference = this.snapPointToGrid(new Point2D.Double(dx, dy), 1.0f);
                EventBus.publish(new LayerAnchorDragged(screenToWorld, selected, snappedDifference));
            }
        } else if (ControlPredicates.layerRotatePredicate.test(e)) {
            if (selected != null) {
                Point2D worldTarget = screenToWorld.transform(e.getPoint(), null);
                EventBus.publish(new LayerRotationQueued(selected, worldTarget));
            }
        } else if (ControlPredicates.changeAnglePredicate.test(e)) {
            if (selected instanceof ShipPainter) {
                Point2D worldTarget = rotatedTransform.transform(e.getPoint(), null);
                EventBus.publish(new SlotAngleChangeQueued(worldTarget));
                EventBus.publish(new EngineAngleChangeQueued(worldTarget));
            }
        } else if (ControlPredicates.changeArcOrSizePredicate.test(e)) {
            if (selected instanceof ShipPainter) {
                Point2D worldTarget = rotatedTransform.transform(e.getPoint(), null);
                EventBus.publish(new SlotArcChangeQueued(worldTarget));
                EventBus.publish(new EngineSizeChangeQueued(worldTarget));
            }
        }
        this.previousPoint.setLocation(x, y);
        this.refreshCursorPosition(e);
    }

    private void tryRadiusDrag(MouseEvent e) {
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D transformed = screenToWorld.transform(e.getPoint(), null);
        if (ControlPredicates.isCursorSnappingEnabled()) {
            transformed = screenToWorld.transform(this.getAdjustedCursor(), null);
        }
        EventBus.publish(new RadiusDragQueued(transformed));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        this.tryRadiusDrag(e);
        this.previousPoint.setLocation(x, y);
        this.parentViewer.setRepaintQueued();
        if (ControlPredicates.getSelectionMode() == PointSelectionMode.CLOSEST) {
            EventBus.publish(new PointSelectQueued(null));
        }
        this.refreshCursorPosition(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int wheelRotation = e.getWheelRotation();
        if (ControlPredicates.rotatePredicate.test(e) && this.rotationEnabled) {
            double toRadians = Math.toRadians(wheelRotation);
            double resultRadians = toRadians * ControlPredicates.ROTATION_SPEED;
            rotateViewer(resultRadians);
            this.rotationDegree -= Math.toDegrees(resultRadians);
            if (this.rotationDegree >= 360) {
                this.rotationDegree -= 360;
            }
            rotationDegree = (rotationDegree + 360) % 360;
            StaticController.updateViewerRotation(-resultRadians, rotationDegree);
            EventBus.publish(new ViewerTransformRotated());
        } else {
            // Calculate the zoom factor - sign of wheel rotation argument determines the direction.
            double d = Math.pow(1 + ControlPredicates.ZOOMING_SPEED, -wheelRotation) - 1;
            double factor = 1.0 + d;
            double max = ControlPredicates.MAXIMUM_ZOOM;
            double min = ControlPredicates.MINIMUM_ZOOM;
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
        }
        this.refreshCursorPosition(e);
    }

    private void rotateExact(double desiredDegrees) {
        double desiredRadians = Math.toRadians(desiredDegrees);
        double current = StaticController.getRotationRadians();
        double radiansChange = current - desiredRadians;
        rotateViewer(radiansChange);
        this.rotationDegree = desiredDegrees;
        StaticController.updateViewerRotation(-radiansChange, desiredDegrees);
        EventBus.publish(new ViewerTransformRotated());
    }

    private void rotateViewer(double angleRadians) {
        Point2D midpoint = parentViewer.getViewerMidpoint();
        this.parentViewer.rotate(midpoint.getX(), midpoint.getY(), angleRadians);
    }

    private void setZoomExact(double level) {
        double oldZoom = this.zoomLevel;
        Point2D viewerMidPoint = parentViewer.getViewerMidpoint();
        double factor = level / oldZoom;
        this.parentViewer.zoom(viewerMidPoint.getX(), viewerMidPoint.getY(), factor, factor);
        this.setZoomLevel(level);
    }

    private void setZoomAtLimit(int x, int y, double limit) {
        double factor = limit / zoomLevel;
        parentViewer.zoom(x, y, factor, factor);
        this.setZoomLevel(limit);
    }

    private void setZoomLevel(double level) {
        this.zoomLevel = level;
        StaticController.setZoomLevel(level);
        EventBus.publish(new ViewerZoomChanged());
    }

    public Point2D getAdjustedCursor() {
        Point mouse = new Point(this.getMousePoint());
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
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D adjusted = this.getAdjustedCursor();
        Point2D corrected = Utility.correctAdjustedCursor(adjusted, screenToWorld);
        EventBus.publish(new ViewerCursorMoved(this.mousePoint, adjusted, corrected));
        if (ControlPredicates.selectPointPredicate.test(event)) {
            boolean appendBoundDown = BoundPointsPainter.isAppendBoundHotkeyPressed();
            boolean insertBoundDown = BoundPointsPainter.isInsertBoundHotkeyPressed();
            boolean slotAngleDown = WeaponSlotPainter.isControlHotkeyStaticPressed();
            boolean engineAngleDown = EngineSlotPainter.isControlHotkeyStaticPressed();
            boolean angleHotkeysDown = slotAngleDown || engineAngleDown;

            Point2D cursor = mousePoint;
            if (ControlPredicates.isCursorSnappingEnabled()) {
                cursor = adjusted;
            }
            if (!appendBoundDown && !insertBoundDown && !angleHotkeysDown) {
                EventBus.publish(new PointDragQueued(screenToWorld, cursor));
            }
        }
        this.parentViewer.setRepaintQueued();
    }

}
