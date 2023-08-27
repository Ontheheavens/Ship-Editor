package oth.shipeditor.components.viewer.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.instrument.ship.ShipInstrument;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.CenterPointPainter;
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
 * @since 30.05.2023
 */
public class ShipCenterPoint extends BaseWorldPoint {

    @Getter @Setter
    private float collisionRadius;

    private final CenterPointPainter parentPainter;

    private final Paint collisionCircleColor = new Color(0xFFDCDC40, true);



    public ShipCenterPoint(Point2D position, float radius, ShipPainter layer, CenterPointPainter parent) {
        super(position, layer);
        this.collisionRadius = radius;
        this.parentPainter = parent;
    }

    @Override
    protected boolean isInteractable() {
        LayerPainter shipPainter = super.getParentLayer();
        if (shipPainter instanceof ShipPainter checkedLayer) {
            CenterPointPainter painter = checkedLayer.getCenterPointPainter();
            return ShipInstrumentsPane.getCurrentMode() == getAssociatedMode() && painter.isInteractionEnabled();
        } else {
            throw new IllegalStateException("Illegal parent layer of ship center point!");
        }
    }

    @Override
    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.COLLISION;
    }

    @Override
    public String getNameForLabel() {
        return "Ship Center";
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        AffineTransform delegateWorldToScreen = getDelegateWorldToScreen();
        delegateWorldToScreen.setTransform(worldToScreen);

        this.paintCollisionCircle(g, delegateWorldToScreen);

        Composite old = null;
        if (parentPainter.getPaintOpacity() != 0.0f) {
            old = Utility.setFullAlpha(g);
        }

        this.paintCenterCross(g, delegateWorldToScreen);

        this.paintCoordsLabel(g, delegateWorldToScreen);

        if (old != null) {
            g.setComposite(old);
        }
    }

    @Override
    protected Color createBaseColor() {
        return new Color(250, 200, 30);
    }

    @Override
    protected Color createHoverColor() {
        return ColorUtilities.getBlendedColor(createBaseColor(),
                createSelectColor(), 0.5f);
    }

    @Override
    @SuppressWarnings("WeakerAccess")
    protected Color createSelectColor() {
        return createBaseColor();
    }

    private void paintCenterCross(Graphics2D g, AffineTransform worldToScreen) {
        Color crossColor = createHoverColor();
        if (this.isPointSelected() && isInteractable()) {
            crossColor = createSelectColor();
        }

        Point2D position = this.getPosition();
        Shape cross = ShapeUtilities.createPerpendicularCross(position, 0.4f);
        Shape transformedCross = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, cross, 12);

        DrawUtilities.drawOutlined(g, transformedCross, crossColor);
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
