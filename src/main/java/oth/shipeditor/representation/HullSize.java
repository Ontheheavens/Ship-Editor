package oth.shipeditor.representation;

import lombok.Getter;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@Getter
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public enum HullSize {

    DEFAULT(FontIcon.of(BoxiconsRegular.DICE_1,
            16, Color.DARK_GRAY), 0, StringValues.DEFAULT),
    FIGHTER(FontIcon.of(BoxiconsRegular.DICE_1,
            16, Color.DARK_GRAY), 0, "Fighter"),
    FRIGATE(FontIcon.of(BoxiconsRegular.DICE_2,
            16, Color.DARK_GRAY), 10, "Frigate"),
    DESTROYER(FontIcon.of(BoxiconsRegular.DICE_3,
            16, Color.DARK_GRAY), 20, "Destroyer"),
    CRUISER(FontIcon.of(BoxiconsRegular.DICE_4,
            16, Color.DARK_GRAY), 30, "Cruiser"),
    CAPITAL_SHIP(FontIcon.of(BoxiconsRegular.DICE_5,
            16, Color.DARK_GRAY), 50, "Capital");

    private final FontIcon icon;

    private final int maxFluxRegulators;

    private final String displayedName;

    HullSize(FontIcon fontIcon, int fluxCap, String name) {
        this.icon = fontIcon;
        this.maxFluxRegulators = fluxCap;
        this.displayedName = name;
    }

    public static HullSize getSizeOfActiveLayer() {
        HullSize size = null;

        var activeLayer = StaticController.getActiveLayer();

        if (activeLayer instanceof ShipLayer shipLayer) {
            ShipHull shipHull = shipLayer.getHull();
            if (shipHull != null) {
                size = shipHull.getHullSize();
            } else {
                var shipData = shipLayer.getShipData();
                HullSpecFile hullSpecFile = shipData.getHullSpecFile();
                String hullSize = hullSpecFile.getHullSize();
                size = HullSize.valueOf(hullSize);
            }
        }

        return size;
    }

}
