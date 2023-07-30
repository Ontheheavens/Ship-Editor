package oth.shipeditor.representation.weapon;

import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
public enum WeaponMount {

    TURRET("TURRET", "Turret"),
    HARDPOINT("HARDPOINT", "Hardpoint"),
    HIDDEN("HIDDEN", "Hidden");

    @Getter
    private final String id;

    @Getter
    private final String displayName;

    WeaponMount(String serialized, String name) {
        this.id = serialized;
        this.displayName = name;
    }

}