package oth.shipeditor.components.viewer.painters.points.weapon;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.graphics.Sprite;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 03.09.2023
 */
@Getter @Setter
public class ProjectilePainter implements Painter {

    private final Sprite projectileSprite;

    private final Point2D projectileCenter;

    private Point2D paintAnchor;

    private float spriteOpacity = 1.0f;

    private double rotationRadians;

    private final Size2D spriteDimensions;

    public ProjectilePainter(Sprite sprite, Point2D center, Size2D size) {
        this.projectileSprite = sprite;
        this.projectileCenter = center;
        this.spriteDimensions = size;
    }

    private void paintContent(Graphics2D g) {
        BufferedImage image = this.projectileSprite.image();
        double x = paintAnchor.getX() - projectileCenter.getX();
        double y = paintAnchor.getY() - projectileCenter.getY();

        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.scale(spriteDimensions.getWidth()/image.getWidth(),
                spriteDimensions.getHeight()/image.getHeight());
        g.drawImage(image, transform, null);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        AffineTransform current = g.getTransform();

        double rotation = this.getRotationRadians();
        double centerX = paintAnchor.getX();
        double centerY = paintAnchor.getY();
        g.rotate(rotation, centerX, centerY);
        this.paintContent(g);

        g.setTransform(current);
    }

}
