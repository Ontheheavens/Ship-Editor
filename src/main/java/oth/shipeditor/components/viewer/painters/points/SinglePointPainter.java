package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
public abstract class SinglePointPainter extends AbstractPointPainter {

    @Getter
    private final ShipPainter parentLayer;

    SinglePointPainter(ShipPainter parent) {
        this.parentLayer = parent;
    }

    @Override
    protected boolean isParentLayerActive() {
        return this.parentLayer.isLayerActive();
    }

    @Override
    public boolean isMirrorable() {
        return false;
    }

    /**
     * Conceptually irrelevant for center points.
     * @return null.
     */
    @Override
    public BaseWorldPoint getMirroredCounterpart(WorldPoint inputPoint) {
        throw new UnsupportedOperationException("Mirrored operations unsupported by SinglePointPainters!");
    }

    @Override
    protected void selectPointConditionally() {
        this.selectPointClosest();
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {}

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!checkVisibility()) return;

        float alpha = this.getPaintOpacity();
        Composite old = Utility.setAlphaComposite(g, alpha);

        this.paintPainterContent(g, worldToScreen, w, h);

        this.paintDelegates(g, worldToScreen, w, h);
        g.setComposite(old);
    }

}
