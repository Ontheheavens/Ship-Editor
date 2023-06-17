package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public abstract class FeaturePoint extends BaseWorldPoint {

    private final Painter composed;

    private final Painter secondPainter;

    FeaturePoint(Point2D pointPosition) {
        this(pointPosition, null);
    }

    FeaturePoint(Point2D pointPosition, LayerPainter layer) {
        super(pointPosition, layer);
        this.secondPainter = createSecondaryPainter();
        this.composed = createComposedPainter();
    }

    private Painter createComposedPainter() {
        return new ComposedPointPainter();
    }

    protected abstract Painter createSecondaryPainter();

    @Override
    public Painter getPainter() {
        return composed;
    }

    private class ComposedPointPainter implements Painter {

        private final Painter pointPainter = FeaturePoint.super.getPointPainter();
        private final Painter secondPainter = FeaturePoint.this.secondPainter;
        private final AffineTransform delegateWorldToScreen = new AffineTransform();

        @Override
        public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
            delegateWorldToScreen.setTransform(worldToScreen);
            secondPainter.paint(g, delegateWorldToScreen, w, h);
            pointPainter.paint(g, worldToScreen, w, h);
        }

    }

}
