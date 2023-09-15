package oth.shipeditor.representation.weapon;

import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
public enum WeaponSize {

    SMALL("SMALL", "Small", 1),
    MEDIUM("MEDIUM", "Medium", 2),
    LARGE("LARGE", "Large", 3);

    @Getter
    private final String id;
    @Getter
    private final String displayName;

    @Getter
    private final int numericSize;

    WeaponSize(String serialized, String name, int numeric) {
        this.id = serialized;
        this.displayName = name;
        this.numericSize = numeric;
    }

    public static int getSizeDifference(WeaponSize firstSize, WeaponSize secondSize) {
        if (firstSize == null || secondSize == null) {
            throw new IllegalArgumentException("Both sizes must be non-null");
        }

        return firstSize.numericSize - secondSize.numericSize;
    }

    public static WeaponSize value(String textValue) {
        if (textValue == null || textValue.isEmpty()) {
            return null;
        } else return WeaponSize.valueOf(textValue);
    }

}
