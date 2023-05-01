package oth.shipeditor.components;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.MouseControl;
import de.javagl.viewer.Predicates;
import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.Utility;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.WorldPoint;

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
@SuppressWarnings("FieldCanBeLocal")
public class ShipViewerControls implements MouseControl {

    private final Viewer viewer;

    private final Predicate<MouseEvent> translatePredicate = Predicates.and(
            InputEventPredicates.buttonDown(3),
            InputEventPredicates.noModifiers()
    );

    private final Predicate<MouseEvent> selectPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.noModifiers()
    );

    private final Predicate<MouseEvent> createPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.shiftDown()
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

    /**
     * Current zooming speed
     */
    private final double zoomingSpeed = 0.15;

    private final double rotationSpeed = 0.4;

    @Getter @Setter
    private WorldPoint selected;

    @Getter @Setter
    private double zoomLevel = 1;

    public ShipViewerControls(Viewer viewer) {
        this.viewer = viewer;
    }

    private void repaintPointsPanel() {
        SwingUtilities.invokeLater(() -> PrimaryWindow.getInstance().getPointsPanel().repaint());
    }

    private PointsPainter getPointsPaint() {
        return PrimaryWindow.getInstance().getShipView().getPointsPainter();
    }

    private ViewerPointsPanel getPointsPanel() {
        return PrimaryWindow.getInstance().getPointsPanel();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    protected Point2D getAdjustedCursor() {
        Point2D anchor = viewer.getWorldToScreen().transform(new Point(0, 0), null);
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
        pressPoint.setLocation(e.getPoint());
        if (createPointPredicate.test(e) && getPointsPanel().getMode() == ViewerPointsPanel.PointsMode.CREATE) {
            Point2D screenPoint = this.getAdjustedCursor();
            AffineTransform screenToWorld = viewer.getScreenToWorld();
            Point2D rounded = Utility.correctAdjustedCursor(screenPoint, screenToWorld);
            if (!painter.pointAtCoordsExists(rounded)) {
                WorldPoint wrapped = new BoundPoint(rounded);
                painter.addPoint(wrapped);
                viewer.repaint();
            }
        }
        if (removePointPredicate.test(e)) {
            WorldPoint toRemove = this.getMousedOver();
            if (toRemove != null) {
                painter.removePoint(toRemove);
            }
            viewer.repaint();
        }
        if (selectPointPredicate.test(e) && getPointsPanel().getMode() == ViewerPointsPanel.PointsMode.SELECT) {
            ViewerPointsPanel viewerPointsPanel = PrimaryWindow.getInstance().getPointsPanel();
            if (mousedOverPoint()) {
                if (this.selected != null) {
                    this.selected.setSelected(false);
                }
                this.selected = this.getMousedOver();
                viewerPointsPanel.getPointContainer().setSelectedValue(this.selected, true);
                this.selected.setSelected(true);
                viewer.repaint();
            } else if (this.selected != null) {
                this.selected.setSelected(false);
                this.selected = null;
                viewerPointsPanel.getPointContainer().setSelectedValue(null, true);
                viewer.repaint();
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
            viewer.translate(dx, dy);
        }
        if (selectPointPredicate.test(e) && selected != null
                && getPointsPanel().getMode() == ViewerPointsPanel.PointsMode.SELECT) {
            Point2D translated = viewer.getScreenToWorld().transform(getAdjustedCursor(), null);
            double roundedX = Math.round(translated.getX() * 2) / 2.0;
            double roundedY = Math.round(translated.getY() * 2) / 2.0;
            this.selected.movePosition(roundedX, roundedY);
            viewer.repaint();
            repaintPointsPanel();
        }
        previousPoint.setLocation(e.getX(), e.getY());
        mousePoint = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePoint = e.getPoint();
        if (rotatePredicate.test(e))
        {
            int dy = e.getY() - previousPoint.y;
            viewer.rotate(
                    pressPoint.x, pressPoint.y,
                    Math.toRadians(dy)*rotationSpeed);
        }
        previousPoint.setLocation(e.getX(), e.getY());
        viewer.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        double d = Math.pow(1+zoomingSpeed, -e.getWheelRotation())-1;
        double factor = 1.0 + d;
        if ((zoomLevel > 1200 && factor > 1 ) || (zoomLevel < 0.2 && factor < 1)) return;
        viewer.zoom(e.getX(), e.getY(), factor, factor);
        zoomLevel *= factor;
    }

}
