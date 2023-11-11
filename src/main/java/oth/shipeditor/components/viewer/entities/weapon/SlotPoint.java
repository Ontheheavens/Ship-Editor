package oth.shipeditor.components.viewer.entities.weapon;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public interface SlotPoint extends SlotData {

    Point2D getPosition();

    double getPaintSizeMultiplier();

    Color getCurrentColor();

    void setCursorInBounds(boolean inBounds);

    WeaponSlotOverride getSkinOverride();

}
