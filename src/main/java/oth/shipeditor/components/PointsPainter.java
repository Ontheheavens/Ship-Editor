package oth.shipeditor.components;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.entities.WorldPoint;

import javax.swing.*;
import java.awt.*;
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
    }

    public void addPoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.add(point);
            getModel().addElement(point);
            delegates.add(point.getPainter());
        });
    }

    public void removePoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.remove(point);
            getModel().removeElement(point);
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
            Color original = g.getColor();
            g.setColor(Color.BLACK);
            Point2D prev = worldToScreen.transform(bPoints.get(bPoints.size() - 1).getPosition(), null);
            for (BoundPoint p : bPoints) {
                Point2D current = worldToScreen.transform(p.getPosition(), null);
                g.drawLine((int) prev.getX(), (int) prev.getY(), (int) current.getX(), (int) current.getY());
                prev = current;
            }
            // Set the color to white for visual convenience.
            g.setColor(Color.WHITE);
            Point2D first = worldToScreen.transform(bPoints.get(0).getPosition(), null);
            g.drawLine((int) prev.getX(), (int) prev.getY(), (int) first.getX(), (int) first.getY());
            g.setColor(original);
        };
    }

    private DefaultListModel<WorldPoint> getModel() {
        return PrimaryWindow.getInstance().getPointsPanel().getModel();
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
