package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.utility.Utility;

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
        this(position, null);
    }

    public BoundPoint(Point2D position, LayerPainter layer) {
        super(position, layer);
        coordsLabel = createCoordsLabelPainter();
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.BOUNDS;
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

    private void paintCoordsLabel(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Point2D coordsPoint = getPosition();
        Point2D toDisplay = this.getCoordinatesForDisplay();
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

            Polygon hexagon = Utility.createHexagon(center, 5);

            Paint old = g.getPaint();
            g.setPaint(createBaseColor());
            g.fillPolygon(hexagon);

            g.setPaint(old);

            this.paintCoordsLabel(g, worldToScreen, w, h);
        };
    }

    @Override
    public String toString() {
        Class<? extends BoundPoint> identity = this.getClass();
        Point2D location = this.getCoordinatesForDisplay();
        return identity.getSimpleName() + " (" + location.getX() + "," + location.getY() + ")";
    }

}
