package oth.shipeditor.components;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.entities.WorldPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class PointsPainter implements Painter {

    @Getter
    private final List<WorldPoint> worldPoints = new ArrayList<>();

    @Getter
    private final List<Painter> delegates;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    /**
     * Create a new painter that is a composition of the given painters
     * @param delegates The delegate painters
     */
    public PointsPainter(Painter ... delegates) {
        this.delegates = new ArrayList<>(Arrays.asList(delegates));
        this.delegateWorldToScreen = new AffineTransform();
    }

    public void addPoint(WorldPoint point) {
        worldPoints.add(point);
        PrimaryWindow.getInstance().getPointsPanel().getModel().addElement(point);
        delegates.add(point.getPainter());
    }

    public void removePoint(WorldPoint point) {
        worldPoints.remove(point);
        PrimaryWindow.getInstance().getPointsPanel().getModel().removeElement(point);
        delegates.remove(point.getPainter());
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
