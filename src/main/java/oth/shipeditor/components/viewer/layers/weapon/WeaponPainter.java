package oth.shipeditor.components.viewer.layers.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.utility.graphics.Sprite;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Sprite field of superclass (which is an image layer painter is initialized with)
 * is assumed to be turret-version of main weapon image.
 * @author Ontheheavens
 * @since 28.07.2023
 */
@Getter @Setter
public class WeaponPainter extends LayerPainter {

    private WeaponMount mount = WeaponMount.TURRET;

    private Sprite turretSprite;

    private Sprite turretUnderSprite;

    private Sprite turretGunSprite;

    private Sprite turretGlowSprite;

    private Sprite hardpointSprite;

    private Sprite hardpointUnderSprite;

    private Sprite hardpointGunSprite;

    private Sprite hardpointGlowSprite;

    public WeaponPainter(ViewerLayer layer) {
        super(layer);
    }

    protected void paintContent(Graphics2D g) {
        if (mount == WeaponMount.TURRET) {
            this.drawSpritePart(g, turretUnderSprite);
            super.paintContent(g);
            this.drawSpritePart(g, turretGunSprite);
            this.drawSpritePart(g, turretGlowSprite);
        } else {
            this.drawSpritePart(g, hardpointUnderSprite);
            this.drawSpritePart(g, hardpointSprite);
            this.drawSpritePart(g, hardpointGunSprite);
            this.drawSpritePart(g, hardpointGlowSprite);
        }
    }

    private void drawSpritePart(Graphics2D g, Sprite part) {
        if (part == null) return;
        Point2D anchor = this.getAnchor();
        BufferedImage spriteImage = part.getSpriteImage();
        int width = spriteImage.getWidth();
        int height = spriteImage.getHeight();
        g.drawImage(spriteImage, (int) anchor.getX(), (int) anchor.getY(), width, height, null);
    }

    @Override
    protected Point2D getRotationAnchor() {
        Point2D result = new Point2D.Double();
        Point2D anchor = this.getAnchor();
        switch (mount) {
            case HARDPOINT -> {
                BufferedImage spriteImage = hardpointSprite.getSpriteImage();
                result = new Point2D.Double(anchor.getX() + spriteImage.getWidth() * 0.5f,
                        anchor.getY() + spriteImage.getHeight() * 0.25f);
            }
            case TURRET, HIDDEN -> {
                BufferedImage spriteImage = turretSprite.getSpriteImage();
                result = new Point2D.Double(anchor.getX() + spriteImage.getWidth() * 0.5f,
                        anchor.getY() + spriteImage.getHeight() * 0.5f);
            }
        }
        return result;
    }

}
