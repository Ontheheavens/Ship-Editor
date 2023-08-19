package oth.shipeditor.components.viewer.entities.engine;

import com.jhlabs.image.HSBAdjustFilter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
@Getter @Setter
public class EnginePoint extends AngledPoint {

    private static final Color SIZING_RECTANGLE = new Color(0, 0, 0, 40);

    private double angle;

    private double length;

    private double width;

    private int contrailSize;

    private EngineStyle style;

    private static final BufferedImage engineFlame;

    private static final BufferedImage engineFlameCore;

    static {
        String flameSprite = "engineflame32.png";
        engineFlame = FileLoading.loadImageResource(flameSprite);
        String flameCoreSprite = "engineflamecore32.png";
        engineFlameCore = FileLoading.loadImageResource(flameCoreSprite);
    }

    public EnginePoint(Point2D pointPosition, ShipPainter layer) {
        super(pointPosition, layer);
    }

    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.ENGINES;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void changeSlotAngle(double degrees) {
        setAngle(degrees);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.drawEngineRectangle(g, worldToScreen);
        super.paint(g, worldToScreen, w, h);
    }

    private void drawEngineRectangle(Graphics2D g, AffineTransform worldToScreen) {
        double transformedAngle = Utility.transformAngle(this.angle);
        Point2D position = this.getPosition();

        double engineWidth = this.width;
        double engineLength = this.length;
        double halfWidth = engineWidth * 0.5f;

        Point2D topLeft = new Point2D.Double(position.getX(), position.getY() - halfWidth);
        Shape sizingRectangle = new Rectangle2D.Double(topLeft.getX(),
                topLeft.getY(), engineLength, engineWidth);

        AffineTransform oldAT = g.getTransform();
        AffineTransform oldWtS = new AffineTransform(worldToScreen);
        AffineTransform rotateInstance = AffineTransform.getRotateInstance(Math.toRadians(transformedAngle),
                position.getX(), position.getY());
        worldToScreen.concatenate(rotateInstance);

        g.transform(worldToScreen);

        DrawUtilities.fillShape(g, sizingRectangle, SIZING_RECTANGLE);
        DrawUtilities.outlineShape(g,sizingRectangle, Color.BLACK, 0.05f);

        Color flameColor = new Color(255,125,25);
        if (style != null) {
            flameColor = style.getEngineColor();
        }

        float[] hue = Color.RGBtoHSB(flameColor.getRed(), flameColor.getGreen(), flameColor.getBlue(), null);
        BufferedImageOp filter = new HSBAdjustFilter(hue[0], hue[1], hue[2]);
        BufferedImage filtered = filter.filter(engineFlame, null);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(filtered, (int) topLeft.getX(), (int) topLeft.getY(),
                (int) engineLength, (int) engineWidth, null);
        g.drawImage(engineFlameCore, (int) topLeft.getX(), (int) topLeft.getY(),
                (int) engineLength, (int) engineWidth, null);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        worldToScreen.setTransform(oldWtS);
        g.setTransform(oldAT);
    }

}
