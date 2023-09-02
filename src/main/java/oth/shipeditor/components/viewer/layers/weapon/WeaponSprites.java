package oth.shipeditor.components.viewer.layers.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.utility.graphics.Sprite;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 02.09.2023
 */
@Getter @Setter
public class WeaponSprites {

    private Sprite turretSprite;

    private Sprite turretUnderSprite;

    private Sprite turretGunSprite;

    private Sprite turretGlowSprite;

    private Sprite hardpointSprite;

    private Sprite hardpointUnderSprite;

    private Sprite hardpointGunSprite;

    private Sprite hardpointGlowSprite;

    public Sprite getMainSprite(WeaponMount mount) {
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointSprite;
        } else {
            return turretSprite;
        }
    }

    public Sprite getUnderSprite(WeaponMount mount) {
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointUnderSprite;
        } else {
            return turretUnderSprite;
        }
    }

    public Sprite getGunSprite(WeaponMount mount) {
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointGunSprite;
        } else {
            return turretGunSprite;
        }
    }

    public Sprite getGlowSprite(WeaponMount mount) {
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointGlowSprite;
        } else {
            return turretGlowSprite;
        }
    }

    Point2D getWeaponCenter(WeaponMount mount) {
        Point2D result = new Point2D.Double();
        final float centerRatio = 0.5f;
        switch (mount) {
            case HARDPOINT -> {
                BufferedImage spriteImage = hardpointSprite.image();
                float centerY = 0.25f;
                result = new Point2D.Double(spriteImage.getWidth() * centerRatio, spriteImage.getHeight() * centerY);
            }
            case TURRET, HIDDEN -> {
                BufferedImage spriteImage = turretSprite.image();
                result = new Point2D.Double(spriteImage.getWidth() * centerRatio, spriteImage.getHeight() * centerRatio);
            }
        }
        return result;
    }

}
