package oth.shipeditor.representation.weapon;

import lombok.Getter;

/**
 * @author Ontheheavens.
 * @since 26.07.2023
 */
public enum WeaponType {

    BALLISTIC("BALLISTIC", "Ballistic"),
    ENERGY("ENERGY", "Energy"),
    MISSILE("MISSILE", "Missile"),
    LAUNCH_BAY("LAUNCH_BAY", "Launch Bay"),
    UNIVERSAL("UNIVERSAL", "Universal"),
    HYBRID("HYBRID", "Hybrid"),
    SYNERGY("SYNERGY", "Synergy"),
    COMPOSITE("COMPOSITE", "Composite"),
    BUILT_IN("BUILT_IN", "Built in"),
    DECORATIVE("DECORATIVE", "Decorative"),
    SYSTEM("SYSTEM", "System"),
    STATION_MODULE("STATION_MODULE", "Station Module");

    @Getter
    private final String id;

    @Getter
    private final String displayName;

    WeaponType(String serialized, String name) {
        this.id = serialized;
        this.displayName = name;
    }

}
