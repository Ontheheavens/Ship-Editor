package oth.shipeditor.components;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.Utility;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.WorldPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class PointsPainter implements Painter {

    @Getter
    private final List<WorldPoint> worldPoints;

    private final List<BoundPoint> boundPoints;
    @Getter
    private final List<Painter> delegates;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    private boolean appendBoundHotkeyPressed = false;
    private boolean insertBoundHotkeyPressed = false;

    private final int appendBoundHotkey = KeyEvent.VK_SHIFT;
    private final int insertBoundHotkey = KeyEvent.VK_CONTROL;

    {
        SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
                delegates.add(createBoundLinesPainter());
            }
        });
    }

    /**
     * Create a new painter that is a composition of the painters in delegate list.
     */
    public PointsPainter() {
        this.delegates = new ArrayList<>();
        this.worldPoints = new ArrayList<>();
        this.boundPoints = new LinkedList<>();
        this.delegateWorldToScreen = new AffineTransform();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (ke.getKeyCode() == appendBoundHotkey) {
                        appendBoundHotkeyPressed = true;
                        repaintViewer();
                        break;
                    }
                    if (ke.getKeyCode() == insertBoundHotkey) {
                        insertBoundHotkeyPressed = true;
                        repaintViewer();
                        break;
                    }
                case KeyEvent.KEY_RELEASED:
                    if (ke.getKeyCode() == appendBoundHotkey) {
                        appendBoundHotkeyPressed = false;
                        repaintViewer();
                        break;
                    }
                    if (ke.getKeyCode() == insertBoundHotkey) {
                        insertBoundHotkeyPressed = false;
                        repaintViewer();
                        break;
                    }
            }
            return false;
        });
    }

    private void repaintViewer() {
        PrimaryWindow.getInstance().getShipView().getViewer().repaint();
    }

    public void addPoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.add(point);
            getPointsPanel().getModel().addElement(point);
            delegates.add(point.getPainter());
        });
    }

    public void insertPoint(WorldPoint toInsert, WorldPoint preceding) {
        int precedingIndex = worldPoints.indexOf(preceding);
        SwingUtilities.invokeLater(() -> {
            worldPoints.add(precedingIndex, toInsert);
            getPointsPanel().getModel().insertElementAt(toInsert, precedingIndex);
            delegates.add(toInsert.getPainter());
        });
    }

    public List<BoundPoint> findClosestBoundPoints(Point2D cursor) {
        List<BoundPoint> boundPoints = this.getBoundPoints();
        BoundPoint closestPoint1 = boundPoints.get(0);
        BoundPoint closestPoint2 = boundPoints.get(1);

        double dist1 = closestPoint1.distance(cursor);
        double dist2 = closestPoint2.distance(cursor);
        if (dist2 < dist1) {
            BoundPoint tmp = closestPoint1;
            closestPoint1 = closestPoint2;
            closestPoint2 = tmp;
            double tmpDist = dist1;
            dist1 = dist2;
            dist2 = tmpDist;
        }

        for (int i = 2; i < boundPoints.size(); i++) {
            BoundPoint currentPoint = boundPoints.get(i);
            double dist = currentPoint.distance(cursor);
            if (dist < dist1) {
                closestPoint2 = closestPoint1;
                closestPoint1 = currentPoint;
                dist2 = dist1;
                dist1 = dist;
            } else if (dist < dist2) {
                closestPoint2 = currentPoint;
                dist2 = dist;
            }
        }

        List<BoundPoint> closestPoints = new ArrayList<>(2);
        closestPoints.add(closestPoint1);
        closestPoints.add(closestPoint2);
        return closestPoints;
    }

    public int getLowestBoundPointIndex(List<BoundPoint> closestPoints) {
        int index1 = worldPoints.indexOf(closestPoints.get(0));
        int index2 = worldPoints.indexOf(closestPoints.get(1));
        return Math.min(index1, index2);
    }

    public void removePoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.remove(point);
            getPointsPanel().getModel().removeElement(point);
            delegates.remove(point.getPainter());
        });

    }

    private List<BoundPoint> getBoundPoints() {
        return worldPoints.stream().filter(p -> p instanceof BoundPoint).map(p -> (BoundPoint) p).toList();
    }

    private Painter createBoundLinesPainter() {
        return (g, worldToScreen, w, h) -> {
            List<BoundPoint> bPoints = getBoundPoints();
            if (bPoints.isEmpty()) return;
            Stroke origStroke = g.getStroke();
            Paint origPaint = g.getPaint();
            Point2D prev = worldToScreen.transform(bPoints.get(bPoints.size() - 1).getPosition(), null);
            for (BoundPoint p : bPoints) {
                Point2D curr = worldToScreen.transform(p.getPosition(), null);
                Utility.drawBorderedLine(g, prev, curr, Color.LIGHT_GRAY);
                prev = curr;
            }
            // Set the color to white for visual convenience.
            Point2D first = worldToScreen.transform(bPoints.get(0).getPosition(), null);
            Utility.drawBorderedLine(g, prev, first, Color.DARK_GRAY);
            ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
            if (this.getPointsPanel().getMode() == ViewerPointsPanel.PointsMode.CREATE) {
                Point2D cursor = viewerPanel.getControls().getAdjustedCursor();
                AffineTransform screenToWorld = viewerPanel.getViewer().getScreenToWorld();
                Point2D adjusted = worldToScreen.transform(Utility.correctAdjustedCursor(cursor, screenToWorld), null);
                if (appendBoundHotkeyPressed) {
                    Utility.drawBorderedLine(g, prev, adjusted, Color.WHITE);
                    Utility.drawBorderedLine(g, adjusted, first, Color.WHITE);
                } else if (insertBoundHotkeyPressed) {
                    List<BoundPoint> closest = this.findClosestBoundPoints(adjusted);
                    Point2D preceding = worldToScreen.transform(closest.get(0).getPosition(), null);
                    Point2D subsequent = worldToScreen.transform(closest.get(1).getPosition(), null);
                    Utility.drawBorderedLine(g, preceding, adjusted, Color.WHITE);
                    Utility.drawBorderedLine(g, subsequent, adjusted, Color.WHITE);
                }
            }
            g.setStroke(origStroke);
            g.setPaint(origPaint);
        };
    }

    private ViewerPointsPanel getPointsPanel() {
        return PrimaryWindow.getInstance().getPointsPanel();
    }

    public boolean pointAtCoordsExists(Point2D point2D) {
        boolean pointDoesExist = false;
        for (WorldPoint wPoint : worldPoints) {
            Point2D coords = wPoint.getPosition();
            if (point2D.equals(coords)) {
                pointDoesExist = true;
                break;
            }
        }
        return pointDoesExist;
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        for (Painter delegate : delegates)
        {
            if (delegate != null)
            {
                delegateWorldToScreen.setTransform(worldToScreen);
                delegate.paint(g, delegateWorldToScreen, w, h);
            }
        }
    }

}
