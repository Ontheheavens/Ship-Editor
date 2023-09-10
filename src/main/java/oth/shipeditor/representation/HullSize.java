package oth.shipeditor.representation;

import lombok.Getter;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.utility.text.StringValues;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public enum HullSize {

    DEFAULT(FontIcon.of(BoxiconsRegular.DICE_1, 16, Color.DARK_GRAY), StringValues.DEFAULT),
    FIGHTER(FontIcon.of(BoxiconsRegular.DICE_1, 16, Color.DARK_GRAY), "Fighter"),
    FRIGATE(FontIcon.of(BoxiconsRegular.DICE_2, 16, Color.DARK_GRAY), "Frigate"),
    DESTROYER(FontIcon.of(BoxiconsRegular.DICE_3, 16, Color.DARK_GRAY), "Destroyer"),
    CRUISER(FontIcon.of(BoxiconsRegular.DICE_4, 16, Color.DARK_GRAY), "Cruiser"),
    CAPITAL_SHIP(FontIcon.of(BoxiconsRegular.DICE_5, 16, Color.DARK_GRAY), "Capital");

    @Getter
    private final FontIcon icon;

    @Getter
    private final String displayedName;

    HullSize(FontIcon fontIcon, String name) {
        this.icon = fontIcon;
        this.displayedName = name;
    }

}
