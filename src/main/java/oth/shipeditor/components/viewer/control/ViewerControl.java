package oth.shipeditor.components.viewer.control;

import de.javagl.viewer.MouseControl;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public interface ViewerControl extends MouseControl {

    Point getMousePoint();

    Point2D getAdjustedCursor();

    void refreshCursorPosition(MouseEvent event);

    void notifyCursorState(Point cursorLocation);

}
