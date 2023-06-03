package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public interface WorldPoint {

    /**
     * @return contained position instance.
     */
    Point2D getPosition();

    /**
     * @param x position on X axis.
     * @param y position on Y axis.
     */
    void setPosition(double x, double y);

    /**
     * @return Painter instance responsible for graphical representation of this point in Viewer.
     */
    Painter getPointPainter();

    /**
     * @return whether this point is selected, that is, active for some user input such as moving the point.
     */
    boolean isSelected();

    /**
     * @param selected whether this point should be selected.
     */
    void setSelected(boolean selected);



}
