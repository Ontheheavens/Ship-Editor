package oth.shipeditor.components.viewer.entities.engine;

import com.jhlabs.image.HSBAdjustFilter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Size2D;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.GraphicsAction;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public class EnginePoint extends AngledPoint {

    private static final Paint SIZING_RECTANGLE = new Color(0, 0, 0, 20);

    @Setter
    private double angle;

    @Getter @Setter
    private double length;

    @Setter
    private double width;

    @Setter
    private int contrailSize;

    @Getter @Setter
    private String styleID;

    private EngineStyle style;

    private static final BufferedImage FLAME;

    private static final BufferedImage FLAME_CORE;

    private BufferedImage flameColored;

    static {
        String flameSprite = "engineflame32.png";
        FLAME = FileLoading.loadImageResource(flameSprite);
        String flameCoreSprite = "engineflamecore32.png";
        FLAME_CORE = FileLoading.loadImageResource(flameCoreSprite);
    }

    public EngineStyle getStyle() {
        if (style != null) return style;
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, EngineStyle> allEngineStyles = gameData.getAllEngineStyles();
        if (allEngineStyles != null) {
            EngineStyle engineStyle = allEngineStyles.get(styleID);
            this.setStyle(engineStyle);
        }
        return style;
    }

    public double getWidth() {
        return width;
    }

    public EnginePoint(Point2D pointPosition, ShipPainter layer) {
        super(pointPosition, layer);
        this.flameColored = FLAME;
        this.setStyle(null);
    }

    public void setSize(Size2D size) {
        this.setLength(size.getHeight());
        this.setWidth(size.getWidth());
    }

    public void changeSize(Size2D size) {
        EditDispatch.postEngineSizeChanged(this, size);
    }

    public double getContrailSize() {
        return contrailSize;
    }

    public Size2D getSize() {
        return new Size2D(this.getWidth(), this.getLength());
    }

    public void setStyle(EngineStyle engineStyle) {
        this.style = engineStyle;

        if (engineStyle != null) {
            this.setStyleID(engineStyle.getEngineStyleID());
        }

        Color flameColor = new Color(255, 125, 25);
        if (engineStyle != null) {
            flameColor = engineStyle.getEngineColor();
        }

        float[] hue = Color.RGBtoHSB(flameColor.getRed(), flameColor.getGreen(), flameColor.getBlue(), null);
        BufferedImageOp filter = new HSBAdjustFilter(hue[0], hue[1], hue[2]);
        flameColored = filter.filter(FLAME, null);
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
        EditDispatch.postEngineAngleSet(this,this.angle,degrees);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.drawEngineRectangle(g, worldToScreen);
        this.drawEngineFlame(g, worldToScreen);
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

        DrawUtilities.drawWithRotationTransform(g, worldToScreen,
                position, Math.toRadians(transformedAngle), graphics2D -> {
                    DrawUtilities.fillShape(g, sizingRectangle, SIZING_RECTANGLE);
                    DrawUtilities.outlineShape(g,sizingRectangle, Color.BLACK, 0.05f);
                });
    }

    private void drawEngineFlame(Graphics2D g, AffineTransform worldToScreen) {
        double transformedAngle = Utility.transformAngle(this.angle);
        Point2D position = this.getPosition();
        double engineWidth = this.width;
        double engineLength = this.length;
        double halfWidth = engineWidth * 0.5f;

        Point2D topLeft = new Point2D.Double(position.getX(), position.getY() - halfWidth);

        GraphicsAction graphicsAction = graphics2D -> {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            AffineTransform transform = new AffineTransform();
            transform.translate(topLeft.getX(), topLeft.getY());
            transform.scale(engineLength/FLAME_CORE.getWidth(), engineWidth/FLAME_CORE.getHeight());

            g.drawImage(flameColored, transform, null);
            g.drawImage(FLAME_CORE, transform, null);

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        };
        DrawUtilities.drawWithRotationTransform(g, worldToScreen, position,
                Math.toRadians(transformedAngle), graphicsAction);
    }


}
