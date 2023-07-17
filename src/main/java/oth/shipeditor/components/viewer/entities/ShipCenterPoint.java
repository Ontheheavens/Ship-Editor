package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.CenterPointPainter;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.05.2023
 */
public class ShipCenterPoint extends FeaturePoint{

    @Getter @Setter
    private float collisionRadius;

    private final Paint collisionCircleColor = new Color(0xFFDCDC40, true);

    public ShipCenterPoint(Point2D position, float radius, LayerPainter layer) {
        super(position, layer);
        this.collisionRadius = radius;
    }

    @Override
    protected boolean isInteractable() {
        LayerPainter layerPainter = super.getParentLayer();
        CenterPointPainter painter = layerPainter.getCenterPointPainter();
        return BaseWorldPoint.getInstrumentationMode() == getAssociatedMode() && painter.isInteractionEnabled();
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.CENTERS;
    }

    @Override
    public String getNameForLabel() {
        return "Ship Center";
    }

    @Override
    protected void adjustLabelPosition(LabelPainter labelPainter) {
        labelPainter.setLabelAnchor(-0.1f, 0.55f);
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            this.paintCollisionCircle(g, worldToScreen);
            Point2D position = this.getPosition();

            int rule = AlphaComposite.SRC_OVER;
            Composite old = g.getComposite();
            Composite opacity = AlphaComposite.getInstance(rule, 1.0f) ;
            g.setComposite(opacity);

            Paint oldPaint = g.getPaint();
            g.setPaint(Color.BLACK);

            Point2D point = new Point2D.Double(position.getX(), position.getY());
            Point2D dest = worldToScreen.transform(point, null);
            float radius = 1.0f;
            Shape dot = Utility.createCircle(dest, radius);

            g.fill(dot);
            g.draw(Utility.createHexagon(worldToScreen.transform(position, null), 10));

            g.setPaint(oldPaint);

            this.paintCoordsLabel(g, worldToScreen, w, h);

            g.setComposite(old);
        };
    }

    @Override
    public Painter getPointPainter() {
        return (g, worldToScreen, w, h) -> {
            int rule = AlphaComposite.SRC_OVER;
            Composite old = g.getComposite();
            Composite opacity = AlphaComposite.getInstance(rule, 1.0f) ;
            g.setComposite(opacity);

            Painter superPointPainter = super.getPointPainter();
            superPointPainter.paint(g, worldToScreen, w, h);

            g.setComposite(old);
        };
    }

    private void paintCollisionCircle(Graphics2D g, AffineTransform worldToScreen) {
        float radius = this.getCollisionRadius();
        Point2D position = getPosition();
        Shape dot = new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
        Shape transformed = worldToScreen.createTransformedShape(dot);

        Paint old = g.getPaint();
        g.setPaint(collisionCircleColor);
        g.fill(transformed);
        g.setPaint(old);
    }

    @Override
    public String toString() {
        return "ShipCenter";
    }

}
