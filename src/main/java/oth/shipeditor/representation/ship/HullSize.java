package oth.shipeditor.representation.ship;

import lombok.Getter;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@Getter
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public enum HullSize {

    DEFAULT(FontIcon.of(BoxiconsRegular.DICE_1,
            16, Themes.getIconColor()), 0, StringValues.DEFAULT),
    FIGHTER(FontIcon.of(BoxiconsRegular.DICE_1,
            16, Themes.getIconColor()), 0, "Fighter"),
    FRIGATE(FontIcon.of(BoxiconsRegular.DICE_2,
            16, Themes.getIconColor()), 10, "Frigate"),
    DESTROYER(FontIcon.of(BoxiconsRegular.DICE_3,
            16, Themes.getIconColor()), 20, "Destroyer"),
    CRUISER(FontIcon.of(BoxiconsRegular.DICE_4,
            16, Themes.getIconColor()), 30, "Cruiser"),
    CAPITAL_SHIP(FontIcon.of(BoxiconsRegular.DICE_5,
            16, Themes.getIconColor()), 50, "Capital");

    private final FontIcon icon;

    private final int maxFluxRegulators;

    private final String displayedName;

    HullSize(FontIcon fontIcon, int fluxCap, String name) {
        this.icon = fontIcon;
        this.maxFluxRegulators = fluxCap;
        this.displayedName = name;
    }

}
