package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.deserialize.ShipTypeHintsDeserializer;
import oth.shipeditor.utility.StringConstants;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 29.06.2023
 */
@Getter @Setter
@SuppressWarnings("TransientFieldInNonSerializableClass")
public class Skin {

    @JsonIgnore
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

    @JsonIgnore
    private final transient boolean base;

    @JsonProperty(StringConstants.BASE_HULL_ID)
    private String baseHullId;

    @JsonProperty(StringConstants.SKIN_HULL_ID)
    private String skinHullId;

    @JsonProperty(StringConstants.SYSTEM_ID)
    private String systemId;

    @JsonProperty(StringConstants.HULL_NAME)
    private String hullName;

    @JsonProperty(StringConstants.HULL_DESIGNATION)
    private String hullDesignation;

    @JsonProperty(StringConstants.RESTORE_TO_BASE_HULL)
    private boolean restoreToBaseHull;

    @JsonProperty(StringConstants.INCOMPATIBLE_WITH_BASE_HULL)
    private boolean incompatibleWithBaseHull;

    @JsonProperty(StringConstants.FLEET_POINTS)
    private int fleetPoints;

    @JsonProperty(StringConstants.ORDNANCE_POINTS)
    private int ordnancePoints;

    @JsonProperty(StringConstants.BASE_VALUE)
    private int baseValue;

    @JsonProperty(StringConstants.SUPPLIES_PER_MONTH)
    private double suppliesPerMonth;

    @JsonProperty(StringConstants.SUPPLIES_TO_RECOVER)
    private double suppliesToRecover;

    @JsonProperty(StringConstants.DESCRIPTION_ID)
    private String descriptionId;

    @JsonProperty(StringConstants.DESCRIPTION_PREFIX)
    private String descriptionPrefix;

    @JsonProperty(StringConstants.COVERS_COLOR)
    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    private Color coversColor;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("tech")
    private String tech;

    @JsonProperty(StringConstants.BUILT_IN_WINGS)
    private List<String> builtInWings;

    @JsonProperty(StringConstants.FIGHTER_BAYS)
    private int fighterBays;

    @JsonProperty(StringConstants.SPRITE_NAME)
    private String spriteName;

    @JsonProperty(StringConstants.BASE_VALUE_MULT)
    private double baseValueMult;

    @JsonDeserialize(using = ShipTypeHintsDeserializer.class)
    @JsonProperty(StringConstants.REMOVE_HINTS)
    private ShipTypeHints[] removeHints;

    @JsonProperty(StringConstants.ADD_HINTS)
    private List<ShipTypeHints> addHints;

    @JsonProperty(StringConstants.REMOVE_WEAPON_SLOTS)
    private List<String> removeWeaponSlots;

    @JsonProperty(StringConstants.REMOVE_ENGINE_SLOTS)
    private List<Integer> removeEngineSlots;

    @JsonProperty(StringConstants.REMOVE_BUILT_IN_MODS)
    private List<String> removeBuiltInMods;

    @JsonProperty(StringConstants.REMOVE_BUILT_IN_WEAPONS)
    private List<String> removeBuiltInWeapons;

    @JsonProperty(StringConstants.BUILT_IN_MODS)
    private List<String> builtInMods;

    @JsonProperty(StringConstants.BUILT_IN_WEAPONS)
    private Map<String, String> builtInWeapons;

    @JsonProperty(StringConstants.WEAPON_SLOT_CHANGES)
    private Map<String, WeaponSlot> weaponSlotChanges;

    @JsonProperty(StringConstants.ENGINE_SLOT_CHANGES)
    private Map<String, EngineSlot> engineSlotChanges;

    @Override
    public String toString() {
        if (base) {
            return "Base hull";
        }
        String designation = tech;
        String techResult = "";
        if (designation != null && !designation.isEmpty()) {
            techResult = " (" + designation + ")";
        }
        return hullName + techResult;
    }

}

