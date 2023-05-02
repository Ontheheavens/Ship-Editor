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
import java.util.List;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class PointsPainter implements Painter {

    @Getter
    private final List<WorldPoint> worldPoints;

    @Getter
    private final List<Painter> delegates;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    private boolean createBoundHotkeyPressed = false;
    private final int createBoundHotkey = KeyEvent.VK_SHIFT;

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
        this.delegateWorldToScreen = new AffineTransform();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
                switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == createBoundHotkey) {
                            createBoundHotkeyPressed = true;
                            repaintViewer();
                        }
                        break;
                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == createBoundHotkey) {
                            createBoundHotkeyPressed = false;
                            repaintViewer();
                        }
                        break;
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

    public void removePoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.remove(point);
            getPointsPanel().getModel().removeElement(point);
            delegates.remove(point.getPainter());
        });

    }

    private Painter createBoundLinesPainter() {
        return (g, worldToScreen, w, h) -> {
            List<WorldPoint> points = getWorldPoints();
            List<BoundPoint> bPoints = points.stream()
                    .filter(p -> p instanceof BoundPoint)
                    .map(p -> (BoundPoint) p).toList();
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
            if (this.getPointsPanel().getMode() == ViewerPointsPanel.PointsMode.CREATE && createBoundHotkeyPressed) {
                Point2D cursor = viewerPanel.getControls().getAdjustedCursor();
                AffineTransform screenToWorld = viewerPanel.getViewer().getScreenToWorld();
                Point2D adjusted = worldToScreen.transform(Utility.correctAdjustedCursor(cursor, screenToWorld), null);
                Utility.drawBorderedLine(g, prev, adjusted, Color.WHITE);
                Utility.drawBorderedLine(g, adjusted, first, Color.WHITE);
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
