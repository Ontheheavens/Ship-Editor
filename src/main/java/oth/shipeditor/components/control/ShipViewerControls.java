package oth.shipeditor.components.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.MouseControl;
import de.javagl.viewer.Predicates;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.BoundPointsPanel;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.entities.WorldPoint;
import oth.shipeditor.components.painters.PointsPainter;
import oth.shipeditor.utility.ChangeDispatchable;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeSupport;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */

@Log4j2
public class ShipViewerControls implements MouseControl, ChangeDispatchable {

    private final ShipViewerPanel parentViewer;

    @Getter @Setter
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

    /**
     * Is functionally connected to boolean flag in point instance,
     * and to JList selection in points panel.
     */
    @Getter @Setter
    private WorldPoint selected;

    private final BoundEditingControl boundControl;

    private final PropertyChangeSupport fieldChangeDispatcher = new PropertyChangeSupport(this);

    @Getter
    private double zoomLevel = 1;

    public ShipViewerControls(ShipViewerPanel parent) {
        this.parentViewer = parent;
        this.rotationEnabled = true;
        this.boundControl = new BoundEditingControl(this);
        SwingUtilities.invokeLater(() ->
                PrimaryWindow.getInstance().getPrimaryMenu().initToggleRotateOption(ShipViewerControls.this));
    }

    private void repaintPointsPanel() {
        if (getPointsPanel() != null) {
            SwingUtilities.invokeLater(() -> getPointsPanel().repaint());
        }
    }

    private PointsPainter getPointsPaint() {
        return PrimaryWindow.getInstance().getShipView().getPointsPainter();
    }

    private BoundPointsPanel getPointsPanel() {
        return PrimaryWindow.getInstance().getPointsPanel();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public PropertyChangeSupport getPCS() {
        return fieldChangeDispatcher;
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

    @Override
    public void mousePressed(MouseEvent e) {
        PointsPainter painter  = getPointsPaint();
        if (!parentViewer.isSpriteLoaded()) {
            return;
        }
        pressPoint.setLocation(e.getPoint());

        this.boundControl.checkForBoundCreation(e, getPointsPanel(),
                painter, parentViewer);

        if (removePointPredicate.test(e)) {
            WorldPoint toRemove = this.getMousedOver();
            if (toRemove != null) {
                painter.removePoint(toRemove);
            }
            parentViewer.repaint();
        }

        if (selectPointPredicate.test(e)) {
//            JList<WorldPoint> pointList = getPointsPanel().getPointContainer();
            if (mousedOverPoint()) {
                if (this.selected != null) {
                    this.selected.setSelected(false);
                }
                this.selected = this.getMousedOver();
//                pointList.setSelectedValue(this.selected, true);
                this.selected.setSelected(true);
                parentViewer.repaint();
            } else if (this.selected != null) {
                this.selected.setSelected(false);
                this.selected = null;
//                pointList.setSelectedValue(null, true);
                parentViewer.repaint();
            }
        }
        repaintPointsPanel();
    }

    private boolean mousedOverPoint() {
        return this.getMousedOver() != null;
    }

    private WorldPoint getMousedOver() {
        PointsPainter painter  = getPointsPaint();
        WorldPoint mousedOver = null;
        for (WorldPoint wPoint : painter.getWorldPoints()) {
            if (wPoint.isCursorInBounds()) {
                mousedOver = wPoint;
            }
        }
        return mousedOver;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (translatePredicate.test(e)) {
            int dx = e.getX() - previousPoint.x;
            int dy = e.getY() - previousPoint.y;
            parentViewer.translate(dx, dy);
        }

        if (selectPointPredicate.test(e) && selected != null) {
            Point2D translated = parentViewer.getScreenToWorld().transform(getAdjustedCursor(), null);
            double roundedX = Math.round(translated.getX() * 2) / 2.0;
            double roundedY = Math.round(translated.getY() * 2) / 2.0;
            this.selected.movePosition(roundedX, roundedY);
            parentViewer.repaint();
            repaintPointsPanel();
        }
        previousPoint.setLocation(e.getX(), e.getY());
        mousePoint = e.getPoint();
        this.sendAdjustedCursorToStatus();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePoint = e.getPoint();
        if (rotatePredicate.test(e) && rotationEnabled) {
            int dy = e.getY() - previousPoint.y;
            parentViewer.rotate(
                    pressPoint.x, pressPoint.y,
                    Math.toRadians(dy)* ROTATION_SPEED);
        }
        previousPoint.setLocation(e.getX(), e.getY());
        parentViewer.repaint();
        this.sendAdjustedCursorToStatus();
    }

    private void sendAdjustedCursorToStatus() {
        SwingUtilities.invokeLater(() -> {
            AffineTransform screenToWorld = parentViewer.getScreenToWorld();
            Point2D adjustedCursor = Utility.correctAdjustedCursor(getAdjustedCursor(), screenToWorld);
            PrimaryWindow.getInstance().getStatusPanel().setCursorCoordsLabel(adjustedCursor);
        });
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
        SwingUtilities.invokeLater(() -> PrimaryWindow.getInstance().getStatusPanel().setZoomLabel(zoomLevel));
    }

}
