package oth.shipeditor.components.viewer.layers.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.painters.features.ProjectilePainter;
import oth.shipeditor.components.viewer.painters.points.WeaponOffsetPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.utility.graphics.Sprite;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Sprite field of superclass (which is an image layer painter is initialized with)
 * is assumed to be turret-version of main weapon image.
 * @author Ontheheavens
 * @since 28.07.2023
 */
public class WeaponPainter extends LayerPainter {

    @Getter @Setter
    private WeaponMount mount = WeaponMount.TURRET;

    @Getter @Setter
    private WeaponSprites weaponSprites;

    @Getter @Setter
    private String weaponID;

    private final WeaponOffsetPainter turretOffsetPainter;

    private final WeaponOffsetPainter hardpointOffsetPainter;

    @Getter @Setter
    private boolean renderBarrelsBelow;

    @Getter @Setter
    private boolean renderLoadedMissiles;

    @SuppressWarnings("unused")
    private boolean drawGlow;

    @Getter @Setter
    private ProjectilePainter projectilePainter;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public WeaponPainter(ViewerLayer layer) {
        super(layer);
        this.weaponSprites = new WeaponSprites();

        this.turretOffsetPainter = new WeaponOffsetPainter(this, WeaponMount.TURRET);
        this.hardpointOffsetPainter = new WeaponOffsetPainter(this, WeaponMount.HARDPOINT);
        var allPainters = getAllPainters();
        allPainters.add(turretOffsetPainter);
        allPainters.add(hardpointOffsetPainter);

        this.setUninitialized(false);
    }

    public WeaponOffsetPainter getOffsetPainter() {
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointOffsetPainter;
        } else {
            return turretOffsetPainter;
        }
    }

    @Override
    public void setAnchor(Point2D inputAnchor) {
        Point2D oldAnchor = this.getAnchor();
        super.setAnchor(inputAnchor);

//        var offsetPainter = getOffsetPainter();
//        var offsetPoints = offsetPainter.getOffsetPoints();
//        offsetPoints.forEach(offsetPoint -> {
//            Point2D difference = new Point2D.Double(oldAnchor.getX() - inputAnchor.getX(),
//                    oldAnchor.getY() - inputAnchor.getY());
//            Point2D oldPosition = offsetPoint.getPosition();
//            offsetPoint.setPosition(oldPosition.getX() - difference.getX(),
//                    oldPosition.getY() - difference.getY());
//        });
    }

    @Override
    public Point2D getEntityCenter() {
        return this.getRotationAnchor();
    }

    @Override
    public BufferedImage getSprite() {
        var mainSprite = weaponSprites.getMainSprite(mount);
        return mainSprite.image();
    }

    protected void paintContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.drawSpritePart(g, weaponSprites.getUnderSprite(mount));

        if (renderBarrelsBelow) {
            this.drawSpritePart(g, weaponSprites.getGunSprite(mount));
            this.drawSpritePart(g, weaponSprites.getMainSprite(mount));
        } else {
            this.drawSpritePart(g, weaponSprites.getMainSprite(mount));
            this.drawSpritePart(g, weaponSprites.getGunSprite(mount));
        }

        this.paintLoadedMissiles(g, worldToScreen, w, h);

        if (drawGlow) {
            this.drawSpritePart(g, weaponSprites.getGlowSprite(mount));
        }
    }

    private void paintLoadedMissiles(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!renderLoadedMissiles || projectilePainter == null) return;
        var offsetPainter = this.getOffsetPainter();
        var offsets = offsetPainter.getOffsetPoints();
        offsets.forEach(offsetPoint -> {
            projectilePainter.setPaintAnchor(offsetPoint.getPosition());
            projectilePainter.setRotationRadians(Math.toRadians(-offsetPoint.getAngle()));
            projectilePainter.setSpriteOpacity(this.getSpriteOpacity());
            projectilePainter.paint(g, worldToScreen, w, h);
        });
    }

    private void drawSpritePart(Graphics2D g, Sprite part) {
        if (part == null) return;
        Point2D anchor = this.getAnchor();
        BufferedImage spriteImage = part.image();
        int width = spriteImage.getWidth();
        int height = spriteImage.getHeight();
        g.drawImage(spriteImage, (int) anchor.getX(), (int) anchor.getY(), width, height, null);
    }

    public Point2D getWeaponCenter() {
        return weaponSprites.getWeaponCenter(mount);
    }

    @Override
    protected Point2D getRotationAnchor() {
        Point2D anchor = this.getAnchor();
        Point2D weaponCenter = weaponSprites.getWeaponCenter(mount);
        return new Point2D.Double(anchor.getX() + weaponCenter.getX(), anchor.getY() + weaponCenter.getY());
    }

}
