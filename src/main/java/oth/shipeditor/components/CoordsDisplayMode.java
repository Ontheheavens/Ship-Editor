package oth.shipeditor.components;

import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
@Getter
public enum CoordsDisplayMode {

    WORLD("World",
            "World: 0,0 at start of coordinate system"),

    SPRITE_CENTER("Sprite",
            "Sprite: 0,0 at selected sprite center"),

    SHIPCENTER_ANCHOR("Entity Center Anchor",
            "Entity Center Anchor: 0,0 at bottom left corner of selected sprite"),

    SHIP_CENTER("Entity Center",
            "Entity Center: 0,0 at designated entity center of selected layer");

    private final String shortName;

    private final String displayedText;

    CoordsDisplayMode(String name, String text) {
        this.shortName = name;
        this.displayedText = text;
    }

}
