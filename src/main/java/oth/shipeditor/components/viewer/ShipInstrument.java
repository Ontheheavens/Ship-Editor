package oth.shipeditor.components.viewer;

import lombok.Getter;
import oth.shipeditor.utility.text.StringValues;

/**
 * @author Ontheheavens
 * @since 11.06.2023
 */
public enum ShipInstrument {
    LAYER(StringValues.LAYER),
    COLLISION(StringValues.COLLISION),
    SHIELD(StringValues.SHIELD),
    BOUNDS("Bounds"),
    WEAPON_SLOTS("Weapon Slots"),
    LAUNCH_BAYS("Launch Bays"),
    ENGINES("Engines"),
    SKIN("Skin");;


    @Getter
    private final String title;

    ShipInstrument(String name) {
        this.title = name;
    }

}
