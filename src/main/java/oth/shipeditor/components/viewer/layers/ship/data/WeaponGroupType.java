package oth.shipeditor.components.viewer.layers.ship.data;

/**
 * @author Ontheheavens
 * @since 31.08.2023
 */
public enum WeaponGroupType {

    LINKED("Linked"),
    ALTERNATING("Alternating");

    private final String displayName;

    WeaponGroupType(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

}
