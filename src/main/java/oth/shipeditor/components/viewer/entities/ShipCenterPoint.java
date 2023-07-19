package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.LabelPainter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.CenterPointPainter;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.05.2023
 */
public class ShipCenterPoint extends FeaturePoint{

    @Getter @Setter
    private float collisionRadius;

    private final CenterPointPainter parentPainter;

    private final Paint collisionCircleColor = new Color(0xFFDCDC40, true);

    public ShipCenterPoint(Point2D position, float radius, LayerPainter layer, CenterPointPainter parent) {
        super(position, layer);
        this.collisionRadius = radius;
        this.parentPainter = parent;
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
        labelPainter.setLabelAnchor(-0.19f, 0.55f);
    }

    @Override
    protected Painter createComposedPainter() {
        AffineTransform delegateWorldToScreen = getDelegateWorldToScreen();
        return (g, worldToScreen, w, h) -> {
            delegateWorldToScreen.setTransform(worldToScreen);

            this.paintCollisionCircle(g, delegateWorldToScreen);

            Composite old = null;
            if (parentPainter.getPaintOpacity() != 0.0f) {
                old = Utility.setFullAlpha(g);
            }

            this.paintCenterCross(g, delegateWorldToScreen);

            this.paintCoordsLabel(g, delegateWorldToScreen, w, h);

            if (old != null) {
                g.setComposite(old);
            }
        };
    }

    @Override
    protected Painter createSecondaryPainter() {
        return null;
    }

    @Override
    protected Color createHoverColor() {
        return new Color(0xFF00329B, true);
    }

    @Override
    @SuppressWarnings({"WeakerAccess"})
    protected Color createSelectColor() {
        return new Color(0xFF0087FF, true);
    }

    private void paintCenterCross(Graphics2D g, AffineTransform worldToScreen) {
        Color crossColor = createHoverColor();
        if (isSelected() && isInteractable()) {
            crossColor = createSelectColor();
        }

        g.setPaint(crossColor);
        Point2D position = this.getPosition();
        Shape cross = ShapeUtilities.createPerpendicularCross(position, 0.4f);
        Shape transformedCross = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, cross, 12);

        DrawUtilities.drawCentroid(g, transformedCross, crossColor);
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
