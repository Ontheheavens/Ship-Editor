package oth.shipeditor.components;

import oth.shipeditor.CoordinatePlanePanel;

import java.awt.*;

public class AxisLine implements Paintable {

    Axis axis;

    public AxisLine(Axis input) {
        this.axis = input;
    }

    @Override
    public void paint(Graphics input) {
        CoordinatePlanePanel plane = CoordinatePlanePanel.getInstance();
        Color prev = input.getColor();
        input.setColor(Color.BLACK);
        Point center =  plane.getSprite().getDefaultCenter();
        int positionY = (int) (center.getY() + (plane.getSprite().getImageHeight() / 2));
        int positionX = (int) (center.getX() + (plane.getSprite().getImageWidth() / 2));
        switch (axis) {
            case X -> {
                input.drawLine(0, positionY, plane.getWidth(), positionY);
                input.drawString(String.valueOf(positionY), 400, 400);
            }
            case Y -> {
                input.drawLine(positionX, 0, positionX, plane.getHeight());
                input.drawString(String.valueOf(positionX), 200, 400);
            }
        }
        input.setColor(prev);
    }

}
