package oth.shipeditor.representation.weapon;

import lombok.Getter;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
public enum WeaponSize {

    // TODO: dynamic getter of icons to account for selection color and size change.

    SMALL("SMALL", "Small",
            FontIcon.of(BoxiconsRegular.DICE_1, 18, Color.DARK_GRAY), 1),
    MEDIUM("MEDIUM", "Medium",
            FontIcon.of(BoxiconsRegular.DICE_2, 18, Color.DARK_GRAY), 2),
    LARGE("LARGE", "Large",
            FontIcon.of(BoxiconsRegular.DICE_3, 18, Color.DARK_GRAY), 3);

    @Getter
    private final String id;
    @Getter
    private final String displayName;

    @Getter
    private final int numericSize;

    @Getter
    private final Icon icon;

    WeaponSize(String serialized, String name, Icon iconImage, int numeric) {
        this.id = serialized;
        this.displayName = name;
        this.numericSize = numeric;
        this.icon = iconImage;
    }

    public static int getSizeDifference(WeaponSize firstSize, WeaponSize secondSize) {
        if (firstSize == null || secondSize == null) {
            throw new IllegalArgumentException("Both sizes must be non-null");
        }

        return firstSize.numericSize - secondSize.numericSize;
    }

    public static WeaponSize value(String textValue) {
        if (textValue == null || textValue.isEmpty()) {
            return null;
        } else return WeaponSize.valueOf(textValue);
    }

}
