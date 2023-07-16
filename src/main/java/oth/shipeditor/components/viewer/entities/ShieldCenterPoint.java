package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.ShieldPointPainter;
import oth.shipeditor.representation.HullStyle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public class ShieldCenterPoint extends FeaturePoint{

    @Getter @Setter
    private float shieldRadius;

    @Getter
    private final HullStyle associatedStyle;

    public ShieldCenterPoint(Point2D position, float radius, LayerPainter layer, HullStyle style) {
        super(position, layer);
        this.shieldRadius = radius;
        this.associatedStyle = style;
    }

    @Override
    protected boolean isInteractable() {
        LayerPainter layerPainter = super.getParentLayer();
        ShieldPointPainter painter = layerPainter.getShieldPointPainter();
        return BaseWorldPoint.getInstrumentationMode() == getAssociatedMode() && painter.isInteractionEnabled();
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.CENTERS;
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            this.paintShieldCircle(g, worldToScreen);
            // TODO!
        };
    }

    private void paintShieldCircle(Graphics2D g, AffineTransform worldToScreen) {
        float radius = this.getShieldRadius();
        Point2D position = getPosition();
        Shape dot = new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
        Shape transformed = worldToScreen.createTransformedShape(dot);

        Paint old = g.getPaint();
        g.setPaint(associatedStyle.getShieldInnerColor());
        g.fill(transformed);
        g.setPaint(old);
    }

    @Override
    public String toString() {
        return "ShieldCenter";
    }

}
