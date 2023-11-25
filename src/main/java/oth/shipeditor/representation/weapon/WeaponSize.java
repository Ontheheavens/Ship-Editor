package oth.shipeditor.representation.weapon;

import lombok.Getter;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.representation.SizeEnum;
import oth.shipeditor.utility.themes.Themes;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
@Getter
public enum WeaponSize implements SizeEnum {

    // TODO: dynamic getter of icons to account for selection color and size change.

    SMALL("SMALL", "Small",
            FontIcon.of(BoxiconsRegular.DICE_1, 19, Themes.getIconColor()), 1),
    MEDIUM("MEDIUM", "Medium",
            FontIcon.of(BoxiconsRegular.DICE_2, 19, Themes.getIconColor()), 2),
    LARGE("LARGE", "Large",
            FontIcon.of(BoxiconsRegular.DICE_3, 19, Themes.getIconColor()), 3);

    private final String id;
    private final String displayedName;

    private final int numericSize;

    private final FontIcon icon;

    WeaponSize(String serialized, String name, FontIcon iconImage, int numeric) {
        this.id = serialized;
        this.displayedName = name;
        this.numericSize = numeric;
        this.icon = iconImage;
    }

    static int getSizeDifference(WeaponSize firstSize, WeaponSize secondSize) {
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
