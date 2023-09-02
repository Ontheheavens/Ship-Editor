package oth.shipeditor.components.viewer.layers.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.painters.points.WeaponOffsetPainter;
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
public class WeaponPainter extends LayerPainter {

    @Getter @Setter
    private WeaponMount mount = WeaponMount.TURRET;

    @Getter @Setter
    private WeaponSprites weaponSprites;

    private final WeaponOffsetPainter turretOffsetPainter;

    private final WeaponOffsetPainter hardpointOffsetPainter;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public WeaponPainter(ViewerLayer layer) {
        super(layer);
        this.weaponSprites = new WeaponSprites();

        this.turretOffsetPainter = new WeaponOffsetPainter(this);
        this.hardpointOffsetPainter = new WeaponOffsetPainter(this);
        var allPainters = getAllPainters();
        allPainters.add(turretOffsetPainter);
        allPainters.add(hardpointOffsetPainter);
    }

    public WeaponOffsetPainter getOffsetPainter() {
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointOffsetPainter;
        } else {
            return turretOffsetPainter;
        }
    }

    @Override
    public Point2D getEntityCenter() {
        return this.getRotationAnchor();
    }

    protected void paintContent(Graphics2D g) {
        this.drawSpritePart(g, weaponSprites.getUnderSprite(mount));
        this.drawSpritePart(g, weaponSprites.getGunSprite(mount));
        this.drawSpritePart(g, weaponSprites.getMainSprite(mount));
        this.drawSpritePart(g, weaponSprites.getGlowSprite(mount));
    }

    private void drawSpritePart(Graphics2D g, Sprite part) {
        if (part == null) return;
        Point2D anchor = this.getAnchor();
        BufferedImage spriteImage = part.image();
        int width = spriteImage.getWidth();
        int height = spriteImage.getHeight();
        g.drawImage(spriteImage, (int) anchor.getX(), (int) anchor.getY(), width, height, null);
    }

    @Override
    protected Point2D getRotationAnchor() {
        Point2D anchor = this.getAnchor();
        Point2D weaponCenter = weaponSprites.getWeaponCenter(mount);
        return new Point2D.Double(anchor.getX() + weaponCenter.getX(), anchor.getY() + weaponCenter.getY());
    }

}
