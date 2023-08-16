package oth.shipeditor.components.viewer.entities.weapon;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public interface SlotPoint extends SlotData{

    Color getCurrentColor();

    void setCursorInBounds(boolean inBounds);

    WeaponSlotOverride getSkinOverride();

}
