package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.ViewerPointsPanel;
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

    @Getter
    private final BoundPointsPainter boundPainter;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    /**
     * Painter responsible for display of base point entities - needs sub-painters for complete visuals.
     */
    public PointsPainter() {
        this.delegates = new ArrayList<>();
        this.worldPoints = new ArrayList<>();
        this.boundPainter = new BoundPointsPainter(this);
        this.delegates.add(boundPainter);
        this.delegateWorldToScreen = new AffineTransform();
    }

    public void addPoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.add(point);
            getPointsPanel().getModel().addElement(point);
            delegates.add(point.getPainter());
            if (point instanceof BoundPoint boundPoint) {
                boundPainter.getBoundPoints().add(boundPoint);
            }
        });
    }

    public void removePoint(WorldPoint point) {
        SwingUtilities.invokeLater(() -> {
            worldPoints.remove(point);
            getPointsPanel().getModel().removeElement(point);
            delegates.remove(point.getPainter());
            if (point instanceof BoundPoint boundPoint) {
                boundPainter.getBoundPoints().remove(boundPoint);
            }
        });

    }

    protected ViewerPointsPanel getPointsPanel() {
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
