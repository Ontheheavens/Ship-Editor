package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.ShieldPointPainter;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.utility.Utility;

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

    private final ShieldPointPainter parentPainter;

    @Getter
    private final HullStyle associatedStyle;

    public ShieldCenterPoint(Point2D position, float radius, LayerPainter layer, HullStyle style,
                             ShieldPointPainter parent) {
        super(position, layer);
        this.shieldRadius = radius;
        this.associatedStyle = style;
        this.parentPainter = parent;
    }

    @Override
    protected boolean isInteractable() {
        return BaseWorldPoint.getInstrumentationMode() == getAssociatedMode() && parentPainter.isInteractionEnabled();
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.CENTERS;
    }

    @Override
    public String getNameForLabel() {
        return "Shield Center";
    }

    @Override
    protected void adjustLabelPosition(LabelPainter labelPainter) {
        labelPainter.setLabelAnchor(-0.1f, 0.55f);
    }

    private Color getDisplayedShieldColor(Color base) {
        float painterOpacity = parentPainter.getPaintOpacity();
        int alpha = Math.round(painterOpacity * 255); // Convert opacity [0.0, 1.0] to alpha [0, 255].
        int red = base.getRed();
        int green = base.getGreen();
        int blue = base.getBlue();
        return new Color(red, green, blue, alpha);
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            this.paintShieldCircle(g, worldToScreen);

            int rule = AlphaComposite.SRC_OVER;
            Composite old = g.getComposite();
            Composite opacity = AlphaComposite.getInstance(rule, 1.0f) ;
            g.setComposite(opacity);

            Paint oldPaint = g.getPaint();
            g.setPaint(Color.BLACK);

            Point2D position = this.getPosition();
            Point2D point = new Point2D.Double(position.getX(), position.getY());
            Point2D dest = worldToScreen.transform(point, null);
            Shape dot = Utility.createCircle(dest, 1.0f);

            g.fill(dot);
            g.draw(Utility.createCircle(worldToScreen.transform(position, null), 8));

            g.setPaint(oldPaint);
            this.paintCoordsLabel(g, worldToScreen, w, h);

            g.setComposite(old);

        };
    }

    private void paintShieldCircle(Graphics2D g, AffineTransform worldToScreen) {
        float radius = this.getShieldRadius();
        Point2D position = getPosition();
        Shape circle = new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
        Shape transformed = worldToScreen.createTransformedShape(circle);

        Paint old = g.getPaint();
        Color inner = associatedStyle.getShieldInnerColor();
        g.setPaint(getDisplayedShieldColor(inner));
        g.fill(transformed);

        Stroke oldStroke = g.getStroke();

        int strokeWidth = 5;
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(associatedStyle.getShieldRingColor());
        g.draw(transformed);

        g.setStroke(oldStroke);
        g.setPaint(old);
    }

    @Override
    public String toString() {
        return "ShieldCenter";
    }

}
