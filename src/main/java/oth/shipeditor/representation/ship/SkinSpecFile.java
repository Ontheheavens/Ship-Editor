package oth.shipeditor.representation.ship;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.deserialize.ShipTypeHintsDeserializer;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.WeaponSlot;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Raw serialization class.
 * @author Ontheheavens
 * @since 29.06.2023
 */
@Getter @Setter
@SuppressWarnings({"TransientFieldInNonSerializableClass", "ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyComplexClass"})
public class SkinSpecFile implements ShipSpecFile {

    @JsonIgnore
    private static final SkinSpecFile NO_SKIN_DATA_SPEC_FILE = new SkinSpecFile(true);

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static final String DEFAULT = StringConstants.DEFAULT_ID;

    @JsonCreator
    public SkinSpecFile() {
        base = false;
    }

    private SkinSpecFile(boolean isBase) {
        base = isBase;
    }

    public static SkinSpecFile empty() {
        return NO_SKIN_DATA_SPEC_FILE;
    }

    @JsonIgnore
    private final transient boolean base;

    @JsonIgnore
    private transient Path filePath;

    @JsonIgnore
    private transient Path containingPackage;

    @JsonIgnore
    private transient Sprite loadedSkinSprite;

    public Sprite getLoadedSkinSprite() {
        if (loadedSkinSprite == null) {
            String skinSpriteName = this.getSpriteName();
            Path skinPackagePath = this.getContainingPackage();

            if (skinSpriteName == null || skinSpriteName.isEmpty()) {
                ShipSpecFile baseHullSpec = GameDataRepository.retrieveSpecByID(baseHullId);
                skinSpriteName = baseHullSpec.getSpriteName();
            }

            Path skinSpriteFilePath = Path.of(skinSpriteName);
            File skinSpriteFile = FileLoading.fetchDataFile(skinSpriteFilePath, skinPackagePath);

            Sprite skinSprite = FileLoading.loadSprite(skinSpriteFile);
            this.setLoadedSkinSprite(skinSprite);
        }
        return loadedSkinSprite;
    }

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

    @JsonAlias(StringConstants.STYLE)
    @JsonProperty("hullStyle")
    private String hullStyle;

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty(StringConstants.RESTORE_TO_BASE_HULL)
    private Boolean restoreToBaseHull;

    @JsonProperty(StringConstants.INCOMPATIBLE_WITH_BASE_HULL)
    private Boolean incompatibleWithBaseHull;

    @JsonProperty(StringConstants.FLEET_POINTS)
    private Integer fleetPoints;

    // TODO: implement later.
    @JsonProperty("maxSpeed")
    private Integer maxSpeed;

    @JsonProperty(StringConstants.ORDNANCE_POINTS)
    private Integer ordnancePoints;

    @JsonProperty(StringConstants.BASE_VALUE)
    private Integer baseValue;

    @JsonProperty("fpMod")
    private Integer fpMod;

    @JsonProperty("shieldEfficiency")
    private Double shieldEfficiency;

    @JsonProperty(StringConstants.SUPPLIES_PER_MONTH)
    private Double suppliesPerMonth;

    @JsonProperty(StringConstants.SUPPLIES_TO_RECOVER)
    private Double suppliesToRecover;

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
    private Integer fighterBays;

    @JsonProperty(StringConstants.SPRITE_NAME)
    private String spriteName;

    @JsonProperty(StringConstants.BASE_VALUE_MULT)
    private Double baseValueMult;

    @JsonProperty("rarity")
    private Double rarity;

    @JsonDeserialize(using = ShipTypeHintsDeserializer.class)
    @JsonProperty(StringConstants.REMOVE_HINTS)
    private List<ShipTypeHints> removeHints;

    @JsonAlias(StringConstants.HINTS)
    @JsonDeserialize(using = ShipTypeHintsDeserializer.class)
    @JsonProperty(StringConstants.ADD_HINTS)
    private List<ShipTypeHints> addHints;

    @JsonProperty(StringConstants.REMOVE_WEAPON_SLOTS)
    private List<String> removeWeaponSlots;

    @JsonProperty(StringConstants.REMOVE_ENGINE_SLOTS)
    private List<Integer> removeEngineSlots;

    @JsonProperty(StringConstants.REMOVE_BUILT_IN_MODS)
    private List<String> removeBuiltInMods;

    @JsonProperty("removeBuiltInWings")
    private List<String> removeBuiltInWings;

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

    @Override
    public String getHullId() {
        return skinHullId;
    }

}

