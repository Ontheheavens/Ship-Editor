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
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public class ShieldCenterPoint extends BaseWorldPoint{

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
        labelPainter.setLabelAnchor(-0.175f, 0.55f);
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
    public Painter createPointPainter() {
        AffineTransform delegateWorldToScreen = getDelegateWorldToScreen();
        return (g, worldToScreen, w, h) -> {
            delegateWorldToScreen.setTransform(worldToScreen);

            this.paintShieldCircle(g, delegateWorldToScreen);

            Composite old = null;
            if (parentPainter.getPaintOpacity() != 0.0f) {
                old = Utility.setFullAlpha(g);
            }

            this.paintShieldCenterCross(g, delegateWorldToScreen);

            this.paintCoordsLabel(g, delegateWorldToScreen, w, h);

            if (old != null) {
                g.setComposite(old);
            }
        };
    }

    @Override
    protected Color createBaseColor() {
        return new Color(0xFF006E28, true);
    }

    @Override
    protected Color createHoverColor() {
        return new Color(0xFF009B37, true);
    }

    @Override
    @SuppressWarnings("WeakerAccess")
    protected Color createSelectColor() {
        return new Color(0xFF00FF73, true);
    }

    private void paintShieldCenterCross(Graphics2D g, AffineTransform worldToScreen) {
        Color crossColor = createHoverColor();
        if (isSelected() && isInteractable()) {
            crossColor = createSelectColor();
        }

        Point2D position = this.getPosition();

        Shape diagonalCross = ShapeUtilities.createDiagonalCross(position, 0.4f);

        Shape transformedCross = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, diagonalCross, 12);

        DrawUtilities.drawCentroid(g, transformedCross, crossColor);
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
