package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class BoundPoint extends FeaturePoint{

    private final LabelPainter coordsLabel;

    public BoundPoint(Point2D position) {
        super(position);
        coordsLabel = createCoordsLabelPainter();
    }

    private LabelPainter createCoordsLabelPainter() {
        LabelPainter painter = new LabelPainter();
        Point2D coords = super.getPosition();
        painter.setLabelLocation(coords.getX(), coords.getY());
        MenuContainer label = new JLabel();
        Font font = label.getFont().deriveFont(0.25f);
        painter.setFont(font);
        painter.setLabelAnchor(-0.25f, 0.55f);
        return painter;
    }

    // TODO: implement selected-from-any-point functionality and the toggle between it and strict selection method.
    //  For this bound point class will need a reference to bounds painter so it'll be able to loop between point list.

    private void paintCoordsLabel(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Point2D coordsPoint = getPosition();
        Point2D toDisplay = BaseWorldPoint.getCoordinatesForDisplay(this);
        coordsLabel.setLabelLocation(coordsPoint.getX(), coordsPoint.getY());
        String coords = "Bound (" + toDisplay.getX() + ", " + toDisplay.getY() + ")";
        coordsLabel.paint(g, worldToScreen, w, h,coords);
    }

    @Override
    protected Color createBaseColor() {
        return super.createHoverColor();
    }

    @Override
    protected Color createHoverColor() {
        return super.createBaseColor();
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            Point2D center = worldToScreen.transform(getPosition(), null);
            int x = (int) center.getX(), y = (int) center.getY(), radius = 5;

            int[] xPoints = new int[6];
            int[] yPoints = new int[6];

            // Calculate the coordinates of the six points of the hexagon.
            for (int i = 0; i < 6; i++) {
                double angle = 2 * Math.PI / 6 * i;
                xPoints[i] = x + (int) (radius * Math.cos(angle));
                yPoints[i] = y + (int) (radius * Math.sin(angle));
            }

            Paint old = g.getPaint();
            g.setPaint(createBaseColor());
            // Draw the filled hexagon.
            g.fillPolygon(xPoints, yPoints, 6);
            g.setPaint(old);

            this.paintCoordsLabel(g, worldToScreen, w, h);
        };
    }

}
