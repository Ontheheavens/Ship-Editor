package oth.shipeditor.components.viewer.painters;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws Strings in world coordinates.
 * @author Ontheheavens
 * @since 22.07.2023
 */
@Getter @Setter
public class TextPainter {

    private Point2D worldPosition;

    private String text;

    private final AffineTransform delegateTransform;

    public TextPainter() {
        this.worldPosition = new Point2D.Double();
        this.delegateTransform = new AffineTransform();
    }

    public void paintText(Graphics2D g, AffineTransform worldToScreen) {
        if (text == null || text.isEmpty()) return;

        Font font = Utility.getOrbitron(16);

        double textScale = 0.025;

        delegateTransform.setTransform(worldToScreen);
        delegateTransform.translate(worldPosition.getX(), worldPosition.getY());
        delegateTransform.scale(textScale, textScale);
        delegateTransform.rotate(StaticController.getRotationRadians());

        GlyphVector glyphVector = font.createGlyphVector(g.getFontRenderContext(), text);
        Shape textShape = glyphVector.getOutline();

        Rectangle2D textBounds = textShape.getBounds2D();
        double heightOffset = textBounds.getHeight() * 0.37;

        delegateTransform.translate(25,heightOffset);

        Shape transformedText = delegateTransform.createTransformedShape(textShape);

        Shape bounds = delegateTransform.createTransformedShape(glyphVector.getLogicalBounds());

        DrawUtilities.paintOutlinedText(g, bounds, transformedText, null);
    }

}
