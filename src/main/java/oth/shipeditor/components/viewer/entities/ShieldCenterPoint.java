package oth.shipeditor.components.viewer.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ShieldPointPainter;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public class ShieldCenterPoint extends BaseWorldPoint {

    @Getter @Setter
    private float shieldRadius;

    private final ShieldPointPainter parentPainter;

    @Getter
    private final HullStyle associatedStyle;

    public ShieldCenterPoint(Point2D position, float radius, ShipPainter layer, HullStyle style,
                             ShieldPointPainter parent) {
        super(position, layer);
        this.shieldRadius = radius;
        this.associatedStyle = style;
        this.parentPainter = parent;
    }

    @Override
    protected boolean isInteractable() {
        return ShipInstrumentsPane.getCurrentMode() == getAssociatedMode() && parentPainter.isInteractionEnabled();
    }

    @Override
    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.SHIELD;
    }

    @Override
    public String getNameForLabel() {
        return "Shield Center";
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
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        AffineTransform delegateWorldToScreen = getDelegateWorldToScreen();
        delegateWorldToScreen.setTransform(worldToScreen);

        this.paintShieldCircle(g, delegateWorldToScreen);

        Composite old = null;
        if (parentPainter.getPaintOpacity() != 0.0f) {
            old = Utility.setFullAlpha(g);
        }

        this.paintShieldCenterCross(g, delegateWorldToScreen);

        this.paintCoordsLabel(g, delegateWorldToScreen);

        if (old != null) {
            g.setComposite(old);
        }
    }

    @Override
    protected Color createBaseColor() {
        return Color.GRAY;
    }

    @Override
    protected Color createHoverColor() {
        return ColorUtilities.getBlendedColor(createBaseColor(),
                createSelectColor(), 0.5f);
    }

    @Override
    @SuppressWarnings("WeakerAccess")
    protected Color createSelectColor() {
        return Color.LIGHT_GRAY;
    }

    private void paintShieldCenterCross(Graphics2D g, AffineTransform worldToScreen) {
        Color crossColor = createHoverColor();
        if (this.isPointSelected() && isInteractable()) {
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
