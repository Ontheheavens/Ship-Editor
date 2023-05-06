package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.Setter;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.Utility;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerPointsPanel;
import oth.shipeditor.components.entities.BoundPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.05.2023
 */
public class BoundPointsPainter implements Painter {

    private final PointsPainter parent;
    @Setter
    private List<BoundPoint> boundPoints;

    private boolean appendBoundHotkeyPressed = false;
    private boolean insertBoundHotkeyPressed = false;

    private final int appendBoundHotkey = KeyEvent.VK_SHIFT;
    private final int insertBoundHotkey = KeyEvent.VK_CONTROL;

    public BoundPointsPainter(PointsPainter parent) {
        this.boundPoints = new ArrayList<>();
        this.parent = parent;
        this.initHotkeys();
    }

    private void repaintViewer() {
        PrimaryWindow.getInstance().getShipView().repaint();
    }

    public List<BoundPoint> getBoundPoints() {
        return boundPoints;
    }

    private void initHotkeys() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            int keyCode = ke.getKeyCode();
            boolean isAppendHotkey = keyCode == appendBoundHotkey;
            boolean isInsertHotkey = keyCode == insertBoundHotkey;
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isAppendHotkey || isInsertHotkey) {
                        setHotkeyState(isAppendHotkey, true);
                        break;
                    }
                case KeyEvent.KEY_RELEASED:
                    if (isAppendHotkey || isInsertHotkey) {
                        setHotkeyState(isAppendHotkey, false);
                        break;
                    }
            }
            repaintViewer();
            return false;
        });
    }

    public void insertPoint(BoundPoint toInsert, BoundPoint preceding) {
        int precedingIndex = parent.getWorldPoints().indexOf(preceding);
        SwingUtilities.invokeLater(() -> {
            parent.getWorldPoints().add(precedingIndex, toInsert);
            parent.getPointsPanel().getModel().insertElementAt(toInsert, precedingIndex);
            parent.getDelegates().add(toInsert.getPainter());
            int precedingBound = boundPoints.indexOf(preceding);
            boundPoints.add(precedingBound, toInsert);
        });
    }

    private void setHotkeyState(boolean isAppendHotkey, boolean state) {
        if (isAppendHotkey) {
            appendBoundHotkeyPressed = state;
        } else {
            insertBoundHotkeyPressed = state;
        }
    }

    public List<BoundPoint> findClosestBoundPoints(Point2D cursor) {
        List<BoundPoint> boundPoints = new ArrayList<>(getBoundPoints());
        boundPoints.add(boundPoints.get(0)); // Add first point to end of list.
        BoundPoint closestPoint1 = boundPoints.get(0);
        BoundPoint closestPoint2 = boundPoints.get(1);
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < boundPoints.size() - 1; i++) {
            BoundPoint currentPoint = boundPoints.get(i);
            BoundPoint nextPoint = boundPoints.get(i+1);
            Line2D segment = new Line2D.Double(currentPoint.getPosition(), nextPoint.getPosition());
            double dist = segment.ptSegDist(cursor);
            if (dist < minDist) {
                closestPoint1 = currentPoint;
                closestPoint2 = nextPoint;
                minDist = dist;
            }
        }
        List<BoundPoint> closestPoints = new ArrayList<>(2);
        closestPoints.add(closestPoint1);
        closestPoints.add(closestPoint2);
        return closestPoints;
    }

    public int getLowestBoundPointIndex(List<BoundPoint> closestPoints) {
        int index1 = getBoundPoints().indexOf(closestPoints.get(0));
        int index2 = getBoundPoints().indexOf(closestPoints.get(1));
        return Math.min(index1, index2);
    }

    public int getHighestBoundPointIndex(List<BoundPoint> closestPoints) {
        int index1 = getBoundPoints().indexOf(closestPoints.get(0));
        int index2 = getBoundPoints().indexOf(closestPoints.get(1));
        return Math.max(index1, index2);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
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
        if (parent.getPointsPanel().getMode() == ViewerPointsPanel.PointsMode.CREATE) {
            Point2D cursor = viewerPanel.getControls().getAdjustedCursor();
            AffineTransform screenToWorld = viewerPanel.getScreenToWorld();
            Point2D adjusted = worldToScreen.transform(Utility.correctAdjustedCursor(cursor, screenToWorld), null);
            if (appendBoundHotkeyPressed) {
                Utility.drawBorderedLine(g, prev, adjusted, Color.WHITE);
                Utility.drawBorderedLine(g, adjusted, first, Color.WHITE);
            } else if (insertBoundHotkeyPressed) {
                cursor = screenToWorld.transform(adjusted, null);
                List<BoundPoint> closest = this.findClosestBoundPoints(cursor);
                Point2D preceding = worldToScreen.transform(closest.get(1).getPosition(), null);
                Point2D subsequent = worldToScreen.transform(closest.get(0).getPosition(), null);
                Utility.drawBorderedLine(g, preceding, adjusted, Color.WHITE);
                Utility.drawBorderedLine(g, subsequent, adjusted, Color.WHITE);
            }
        }
        g.setStroke(origStroke);
        g.setPaint(origPaint);
    }

}
