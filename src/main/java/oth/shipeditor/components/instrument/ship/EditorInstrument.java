package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import oth.shipeditor.utility.text.StringValues;

/**
 * @author Ontheheavens
 * @since 11.06.2023
 */
@Getter
public enum EditorInstrument {

    LAYER(StringValues.LAYER),
    COLLISION(StringValues.COLLISION),
    SHIELD(StringValues.SHIELD),
    BOUNDS("Bounds"),
    WEAPON_SLOTS("Weapon Slots"),
    LAUNCH_BAYS("Launch Bays"),
    ENGINES("Engines"),
    BUILT_IN_MODS("Built-in Mods"),
    BUILT_IN_WINGS("Built-in Wings"),
    BUILT_IN_WEAPONS("Built-in Weapons"),
    DECORATIVES("Decoratives"),
    SKIN_DATA("Skin: Data"),
    SKIN_SLOTS("Skin: Slots"),
    VARIANT_DATA("Variant: Data"),
    VARIANT_WEAPONS("Variant: Weapons"),
    VARIANT_MODULES("Variant: Modules");

    private final String title;

    EditorInstrument(String name) {
        this.title = name;
    }

}
