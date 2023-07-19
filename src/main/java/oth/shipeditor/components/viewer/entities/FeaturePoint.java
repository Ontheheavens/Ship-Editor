package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
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

    @Getter
    private final AffineTransform delegateWorldToScreen = new AffineTransform();

    @SuppressWarnings("unused")
    FeaturePoint(Point2D pointPosition) {
        this(pointPosition, null);
    }

    FeaturePoint(Point2D pointPosition, LayerPainter layer) {
        super(pointPosition, layer);
        this.composed = createComposedPainter();
    }

    protected Painter createComposedPainter() {
        Painter pointPainter = this.getPointPainter();
        Painter secondaryPainter = this.createSecondaryPainter();
        return (g, worldToScreen, w, h) -> {
            delegateWorldToScreen.setTransform(worldToScreen);
            pointPainter.paint(g, delegateWorldToScreen, w, h);
            secondaryPainter.paint(g, delegateWorldToScreen, w, h);
        };
    }

    protected abstract Painter createSecondaryPainter();

    @Override
    public Painter getPainter() {
        return composed;
    }

}
