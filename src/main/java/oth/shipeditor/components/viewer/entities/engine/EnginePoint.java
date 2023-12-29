package oth.shipeditor.components.viewer.entities.engine;

import com.jhlabs.image.HSBAdjustFilter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.ship.EngineStyle;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.objects.Size2D;
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
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods"})
public class EnginePoint extends AngledPoint implements EngineData {

    private static final Paint SIZING_RECTANGLE = new Color(0, 0, 0, 20);

    @Setter
    private double angle;

    @Setter
    private double length;

    @Setter
    private double width;

    @Setter
    private int contrailSize;

    @Getter @Setter
    private boolean styleIsCustom;

    @Getter @Setter
    private String styleID;

    private EngineStyle style;

    /**
     * Purely for serialization compatibility purposes; supporting inline styles editing seems counterproductive,
     * as they are an anti-pattern anyway.
     */
    @Getter @Setter
    private EngineStyle customStyleSpec;

    @Getter
    private EngineDataOverride skinOverride;

    private static final BufferedImage FLAME;

    private static final BufferedImage FLAME_CORE;

    private static final EngineDrawAction drawAction = new EngineDrawAction();

    private BufferedImage flameColored;

    static {
        String flameSprite = "engineflame32.png";
        FLAME = FileLoading.loadImageResource(flameSprite);
        String flameCoreSprite = "engineflamecore32.png";
        FLAME_CORE = FileLoading.loadImageResource(flameCoreSprite);
    }

    public void setSkinOverride(EngineDataOverride override) {
        this.skinOverride = override;
        if (skinOverride != null) {
            EngineStyle overrideStyle = skinOverride.getStyle();
            this.setSkinStyleOverride(overrideStyle);
        } else {
            this.setStyle(style);
        }
    }

    public static BufferedImage getBaseFlameTexture() {
        return FLAME;
    }

    @Override
    public Double getAngleBoxed() {
        return angle;
    }

    @Override
    public Double getLengthBoxed() {
        return length;
    }

    @Override
    public Double getWidthBoxed() {
        return width;
    }

    public EngineStyle getStyle() {
        if (this.skinOverride != null && skinOverride.getStyle() != null) {
            return this.skinOverride.getStyle();
        }
        if (style != null) return style;
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, EngineStyle> allEngineStyles = gameData.getAllEngineStyles();
        if (allEngineStyles != null) {
            EngineStyle engineStyle = allEngineStyles.get(styleID);
            this.setStyle(engineStyle);
        }
        return style;
    }

    @Override
    public double getAngle() {
        if (this.skinOverride != null && skinOverride.getAngle() != null) {
            return skinOverride.getAngle();
        }
        return angle;
    }

    public double getWidth() {
        if (this.skinOverride != null && skinOverride.getWidth() != null) {
            return skinOverride.getWidth();
        }
        return width;
    }

    public double getLength() {
        if (this.skinOverride != null && skinOverride.getLength() != null) {
            return skinOverride.getLength();
        }
        return length;
    }

    public EnginePoint(Point2D pointPosition, ShipPainter layer) {
        this(pointPosition, layer, null);
    }

    public EnginePoint(Point2D pointPosition, ShipPainter layer, EnginePoint valuesSource) {
        super(pointPosition, layer);
        this.flameColored = FLAME;
        this.setStyle(null);
        if (valuesSource != null) {
            this.setAngle(valuesSource.getAngle());
            this.setWidth(valuesSource.getWidth());
            this.setLength(valuesSource.getLength());
            this.setContrailSize((int) valuesSource.getContrailSize());
            this.setStyleID(valuesSource.getStyleID());
            this.setStyle(valuesSource.getStyle());
        }
    }

    public void setSize(Size2D size) {
        this.setLength(size.getHeight());
        this.setWidth(size.getWidth());
    }

    public void changeSize(Size2D size) {
        EditDispatch.postEngineSizeChanged(this, size);
    }

    public void changeContrailSize(int contrail) {
        EditDispatch.postEngineContrailChanged(this, contrail);
    }

    public void changeStyle(EngineStyle engineStyle) {
        EditDispatch.postEngineStyleChanged(this, engineStyle);
    }

    public double getContrailSize() {
        return contrailSize;
    }

    @Override
    public Double getContrailSizeBoxed() {
        return (double) contrailSize;
    }

    public Size2D getSize() {
        return new Size2D(this.getWidth(), this.getLength());
    }

    private void setSkinStyleOverride(EngineStyle engineStyle) {
        handleStyleFlameImage(engineStyle);
    }

    public void setStyle(EngineStyle engineStyle) {
        this.style = engineStyle;

        this.styleIsCustom = false;

        if (engineStyle != null) {
            this.setStyleID(engineStyle.getEngineStyleID());
        }
        handleStyleFlameImage(engineStyle);
    }

    private void handleStyleFlameImage(EngineStyle engineStyle) {
        Color flameColor = new Color(255, 125, 25);
        if (engineStyle != null) {
            flameColor = engineStyle.getEngineColor();
        }

        float[] hue = Color.RGBtoHSB(flameColor.getRed(), flameColor.getGreen(), flameColor.getBlue(), null);
        BufferedImageOp filter = new HSBAdjustFilter(hue[0], hue[1], hue[2]);
        flameColored = filter.filter(FLAME, null);
    }

    public EditorInstrument getAssociatedMode() {
        return EditorInstrument.ENGINES;
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
        double rawAngle = this.getAngle();
        Point2D position = this.getPosition();
        double engineWidth = this.getWidth();
        double engineLength = this.getLength();

        EnginePoint.drawRectangleStatically(g, worldToScreen, position, rawAngle, engineWidth, engineLength);
    }

    private void drawEngineFlame(Graphics2D g, AffineTransform worldToScreen) {
        double rawAngle = this.getAngle();
        Point2D position = this.getPosition();
        double engineWidth = this.getWidth();
        double engineLength = this.getLength();

        EnginePoint.drawFlameStatically(g, worldToScreen, position, rawAngle, engineWidth, engineLength, flameColored);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    public static void drawFlameStatically(Graphics2D g, AffineTransform worldToScreen, Point2D position,
                                           double rawAngle, double engineWidth, double engineLength,
                                           BufferedImage flameColored) {
        double transformedAngle = Utility.transformAngle(rawAngle);
        double halfWidth = engineWidth * 0.5f;

        Point2D topLeft = new Point2D.Double(position.getX(), position.getY() - halfWidth);

        drawAction.configureGraphics(g,topLeft, engineLength, engineWidth, flameColored);
        DrawUtilities.drawWithRotationTransform(g, worldToScreen, position,
                Math.toRadians(transformedAngle), drawAction);
    }

    public static void drawRectangleStatically(Graphics2D g, AffineTransform worldToScreen, Point2D position,
                                               double rawAngle, double engineWidth, double engineLength) {
        double transformedAngle = Utility.transformAngle(rawAngle);
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

    @SuppressWarnings("ParameterHidesMemberVariable")
    private static class EngineDrawAction implements GraphicsAction {

        private Graphics2D g;
        private Point2D topLeft;
        private double engineLength;
        private double engineWidth;
        private BufferedImage flameColored;
        private final AffineTransform affineTransform = new AffineTransform();

        void configureGraphics(Graphics2D g, Point2D topLeft, double engineLength,
                               double engineWidth, BufferedImage flameColored) {
            this.g = g;
            this.topLeft = topLeft;
            this.engineLength = engineLength;
            this.engineWidth = engineWidth;
            this.flameColored = flameColored;
        }

        @Override
        public void draw(Graphics2D graphics2D) {
            RenderingHints hints = g.getRenderingHints();

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

            if (FLAME_CORE == null) {
                throw new IllegalStateException("Flame image not loaded!");
            }

            AffineTransform transform = affineTransform;
            transform.translate(topLeft.getX(), topLeft.getY());
            transform.scale(engineLength / FLAME_CORE.getWidth(), engineWidth / FLAME_CORE.getHeight());

            g.drawImage(flameColored, transform, null);
            g.drawImage(FLAME_CORE, transform, null);

            g.setRenderingHints(hints);
            transform.setToIdentity();
        }

    }

}
