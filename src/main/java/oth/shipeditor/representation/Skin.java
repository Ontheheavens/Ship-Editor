package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import oth.shipeditor.parsing.deserialize.ShipTypeHintsDeserializer;
import oth.shipeditor.utility.StringConstants;

import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 29.06.2023
 */
public class Skin {

    private static final Skin NO_SKIN = new Skin(true);

    @JsonCreator
    public Skin() {
        base = false;
    }

    private Skin(boolean isBase) {
        base = isBase;
    }

    public static Skin empty() {
        return NO_SKIN;
    }

    @Getter
    private final boolean base;

    @JsonProperty("baseHullId")
    private String baseHullId;

    @JsonProperty("skinHullId")
    private String skinHullId;

    @JsonProperty("systemId")
    private String systemId;

    @JsonProperty(StringConstants.HULL_NAME)
    private String hullName;

    @JsonProperty("hullDesignation")
    private String hullDesignation;

    @JsonProperty("restoreToBaseHull")
    private boolean restoreToBaseHull;

    @JsonProperty("incompatibleWithBaseHull")
    private boolean incompatibleWithBaseHull;

    @JsonProperty("fleetPoints")
    private int fleetPoints;

    @JsonProperty("ordnancePoints")
    private int ordnancePoints;

    @JsonProperty("descriptionId")
    private String descriptionId;

    @JsonProperty("descriptionPrefix")
    private String descriptionPrefix;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("tech")
    private String tech;

    @JsonProperty(StringConstants.BUILT_IN_WINGS)
    private List<String> builtInWings;

    @JsonProperty("fighterBays")
    private int fighterBays;

    @JsonProperty(StringConstants.SPRITE_NAME)
    private String spriteName;

    @JsonProperty("baseValueMult")
    private double baseValueMult;

    @JsonDeserialize(using = ShipTypeHintsDeserializer.class)
    @JsonProperty("removeHints")
    private ShipTypeHints[] removeHints;

    @JsonProperty("addHints")
    private List<ShipTypeHints> addHints;

    @JsonProperty("removeWeaponSlots")
    private List<String> removeWeaponSlots;

    @JsonProperty("removeEngineSlots")
    private List<Integer> removeEngineSlots;

    @JsonProperty("removeBuiltInMods")
    private List<String> removeBuiltInMods;

    @JsonProperty("removeBuiltInWeapons")
    private List<String> removeBuiltInWeapons;

    @JsonProperty(StringConstants.BUILT_IN_MODS)
    private List<String> builtInMods;

    @JsonProperty(StringConstants.BUILT_IN_WEAPONS)
    private Map<String, String> builtInWeapons;

    @JsonProperty("weaponSlotChanges")
    private Map<String, WeaponSlotChange> weaponSlotChanges;

    static class WeaponSlotChange {

        @JsonProperty("type")
        private String type;

        @JsonProperty("angle")
        private Double angle;

        @JsonProperty("arc")
        private Integer arc;

        @JsonProperty("mount")
        private String mount;

        @JsonProperty("size")
        private String size;
    }

}

