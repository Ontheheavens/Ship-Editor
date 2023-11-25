package oth.shipeditor.representation.ship;

import lombok.Getter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.representation.SizeEnum;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@Getter
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public enum HullSize implements SizeEnum {

    DEFAULT(BoxiconsRegular.DICE_1, 0, StringValues.DEFAULT),
    FIGHTER(BoxiconsRegular.DICE_1, 0, "Fighter"),
    FRIGATE(BoxiconsRegular.DICE_2, 10, "Frigate"),
    DESTROYER(BoxiconsRegular.DICE_3, 20, "Destroyer"),
    CRUISER(BoxiconsRegular.DICE_4, 30, "Cruiser"),
    CAPITAL_SHIP(BoxiconsRegular.DICE_5, 50, "Capital");

    private final Ikon ikonTemplate;

    private final FontIcon icon;

    private final int maxFluxRegulators;

    private final String displayedName;

    public FontIcon getResizedIcon(int size) {
        return FontIcon.of(ikonTemplate, size, Themes.getIconColor());
    }

    HullSize(Ikon ikon, int fluxCap, String name) {
        this.ikonTemplate = ikon;
        this.icon = FontIcon.of(ikonTemplate, 16, Themes.getIconColor());
        this.maxFluxRegulators = fluxCap;
        this.displayedName = name;
    }

}
