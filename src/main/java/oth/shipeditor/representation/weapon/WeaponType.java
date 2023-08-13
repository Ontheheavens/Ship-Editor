package oth.shipeditor.representation.weapon;

import lombok.Getter;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.*;

/**
 * @author Ontheheavens.
 * @since 26.07.2023
 */
public enum WeaponType {

    BALLISTIC("BALLISTIC", "Ballistic", new Color(255,215,0,255)),
    ENERGY("ENERGY", "Energy", new Color(70,200,255,255)),
    MISSILE("MISSILE", "Missile", new Color(155,255,0,255)),
    LAUNCH_BAY(StringConstants.LAUNCH_BAY, "Launch Bay", new Color(255, 255, 255, 255)),
    UNIVERSAL("UNIVERSAL", "Universal", new Color(235, 235, 235,255)),
    HYBRID("HYBRID", "Hybrid", new Color(255,165,0,255)),
    SYNERGY("SYNERGY", "Synergy", new Color(0,255,200,255)),
    COMPOSITE("COMPOSITE", "Composite", new Color(215,255,0,255)),
    BUILT_IN("BUILT_IN", "Built in", new Color(195, 195, 195, 255)),
    DECORATIVE("DECORATIVE", "Decorative", new Color(255, 0, 0,255)),
    SYSTEM("SYSTEM", "System", new Color(145, 145, 145, 255)),
    STATION_MODULE("STATION_MODULE", "Station Module", new Color(170, 0, 255,255));

    @Getter
    private final String id;

    @Getter
    private final Color color;

    @Getter
    private final String displayName;

    WeaponType(String serialized, String name, Color tone) {
        this.id = serialized;
        this.displayName = name;
        this.color = tone;
    }

    public static WeaponType value(String textValue) {
        if (textValue == null || textValue.isEmpty()) {
            return null;
        } else return WeaponType.valueOf(textValue);
    }

}
