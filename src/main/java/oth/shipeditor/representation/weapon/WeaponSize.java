package oth.shipeditor.representation.weapon;

import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
public enum WeaponSize {

    SMALL("SMALL", "Small"),
    MEDIUM("MEDIUM", "Medium"),
    LARGE("LARGE", "Large");

    @Getter
    private final String id;
    @Getter
    private final String displayName;

    WeaponSize(String serialized, String name) {
        this.id = serialized;
        this.displayName = name;
    }

}
