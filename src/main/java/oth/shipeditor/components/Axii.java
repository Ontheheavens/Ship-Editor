package oth.shipeditor.components;

import oth.shipeditor.CoordinatePlanePanel;

import java.awt.*;

/**
 * Contains visual display of axial X and Y lines.
 */
public class Axii implements Paintable {

    @Override
    public void paint(Graphics input) {
        CoordinatePlanePanel plane = CoordinatePlanePanel.getInstance();
        Color prev = input.getColor();
        input.setColor(Color.BLACK);
        Point center =  plane.getCoordinateCenter();
        int positionX = center.x;
        int positionY = center.y;
        input.drawLine(0, positionY, plane.getWidth(), positionY);
        input.drawLine(positionX, 0, positionX, plane.getHeight());
        input.setColor(prev);
    }

}
