package oth.shipeditor.components.control;

import de.javagl.viewer.MouseControl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public interface ViewerControl extends MouseControl {

    Point getMousePoint();

    Point2D getAdjustedCursor();

}
