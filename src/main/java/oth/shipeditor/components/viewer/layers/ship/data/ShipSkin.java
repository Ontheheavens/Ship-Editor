package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.entities.engine.EngineDataOverride;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.*;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponSlot;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

/**
 * @author Ontheheavens
 * @since 31.07.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Getter
public final class ShipSkin {

    private final boolean base;

    public static final ShipSkin EMPTY = new ShipSkin();

    public ShipSkin() {
        this(true);
    }

    private ShipSkin(boolean isBase) {
        this.base = isBase;
    }

    private Path skinFilePath;

    public String getFileName() {
        if (skinFilePath == null) return StringValues.EMPTY;
        return skinFilePath.getFileName().toString();
    }

    private Path containingPackage;

    private Sprite loadedSkinSprite;

    private String baseHullId;

    private String skinHullId;

    private ShipSystemCSVEntry shipSystem;

    private String hullName;

    private String hullDesignation;

    private HullStyle hullStyle;

    private boolean restoreToBaseHull;

    private boolean incompatibleWithBaseHull;

    private int fleetPoints;

    private int ordnancePoints;

    private int baseValue;

    private double suppliesPerMonth;

    private double suppliesToRecover;

    private String descriptionId;

    private String descriptionPrefix;

    private Color coversColor;

    private List<String> tags;

    private String tech;

    private List<WingCSVEntry> builtInWings;

    private int fighterBays;

    private String spriteName;

    private double baseValueMult;

    private List<ShipTypeHints> removeHints;

    private List<ShipTypeHints> addHints;

    private List<String> removeWeaponSlots;

    private List<Integer> removeEngineSlots;

    private List<HullmodCSVEntry> removeBuiltInMods;

    private List<String> removeBuiltInWeapons;

    private List<HullmodCSVEntry> builtInMods;

    private Map<String, WeaponCSVEntry> builtInWeapons;

    private Map<String, WeaponSlotOverride> weaponSlotChanges;

    private Map<Integer, EngineDataOverride> engineSlotChanges;

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

    @SuppressWarnings({"PublicInnerClass", "ClassWithTooManyMethods",
            "unused", "BooleanParameter", "MethodParameterNamingConvention", "WeakerAccess"})
    public static class Builder {
        private ShipSkin skin;

        public Builder() {
            skin = new ShipSkin(false);
        }

        public Builder withSkinFilePath(Path skinFilePath) {
            skin.skinFilePath = skinFilePath;
            return this;
        }

        public Builder withContainingPackage(Path containingPackage) {
            skin.containingPackage = containingPackage;
            return this;
        }

        public Builder withLoadedSkinSprite(Sprite loadedSkinSprite) {
            skin.loadedSkinSprite = loadedSkinSprite;
            return this;
        }

        public Builder withBaseHullId(String baseHullId) {
            skin.baseHullId = baseHullId;
            return this;
        }

        public Builder withSkinHullId(String skinHullId) {
            skin.skinHullId = skinHullId;
            return this;
        }

        public Builder withShipSystem(String systemId) {
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, ShipSystemCSVEntry> allShipsystemEntries = gameData.getAllShipsystemEntries();
            skin.shipSystem = allShipsystemEntries.get(systemId);
            return this;
        }

        public Builder withHullName(String hullName) {
            skin.hullName = hullName;
            return this;
        }

        public Builder withHullDesignation(String hullDesignation) {
            skin.hullDesignation = hullDesignation;
            return this;
        }

        public Builder withHullStyle(String hullStyle) {
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, HullStyle> allHullStyles = gameData.getAllHullStyles();
            if (allHullStyles == null) return this;
            skin.hullStyle = allHullStyles.get(hullStyle);
            return this;
        }

        public Builder withRestoreToBaseHull(boolean restoreToBaseHull) {
            skin.restoreToBaseHull = restoreToBaseHull;
            return this;
        }

        public Builder withIncompatibleWithBaseHull(boolean incompatibleWithBaseHull) {
            skin.incompatibleWithBaseHull = incompatibleWithBaseHull;
            return this;
        }

        public Builder withFleetPoints(int fleetPoints) {
            skin.fleetPoints = fleetPoints;
            return this;
        }

        public Builder withOrdnancePoints(int ordnancePoints) {
            skin.ordnancePoints = ordnancePoints;
            return this;
        }

        public Builder withBaseValue(int baseValue) {
            skin.baseValue = baseValue;
            return this;
        }

        public Builder withSuppliesPerMonth(double suppliesPerMonth) {
            skin.suppliesPerMonth = suppliesPerMonth;
            return this;
        }

        public Builder withSuppliesToRecover(double suppliesToRecover) {
            skin.suppliesToRecover = suppliesToRecover;
            return this;
        }

        public Builder withDescriptionId(String descriptionId) {
            skin.descriptionId = descriptionId;
            return this;
        }

        public Builder withDescriptionPrefix(String descriptionPrefix) {
            skin.descriptionPrefix = descriptionPrefix;
            return this;
        }

        public Builder withCoversColor(Color coversColor) {
            skin.coversColor = coversColor;
            return this;
        }

        public Builder withTags(List<String> tags) {
            skin.tags = tags;
            return this;
        }

        public Builder withTech(String tech) {
            skin.tech = tech;
            return this;
        }

        public Builder withBuiltInWings(Collection<String> builtInWings) {
            if (builtInWings == null) {
                skin.builtInWings = new ArrayList<>();
                return this;
            }
            List<WingCSVEntry> wingEntries = new ArrayList<>(builtInWings.size());
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, WingCSVEntry> allWingEntries = gameData.getAllWingEntries();
            builtInWings.forEach(wingID -> {
                WingCSVEntry entry = allWingEntries.get(wingID);
                wingEntries.add(entry);
            });
            skin.builtInWings = wingEntries;
            return this;
        }

        public Builder withFighterBays(int fighterBays) {
            skin.fighterBays = fighterBays;
            return this;
        }

        public Builder withSpriteName(String spriteName) {
            skin.spriteName = spriteName;
            return this;
        }

        public Builder withBaseValueMult(double baseValueMult) {
            skin.baseValueMult = baseValueMult;
            return this;
        }

        public Builder withRemoveHints(List<ShipTypeHints> removeHints) {
            if (removeHints == null) return this;
            skin.removeHints = removeHints;
            return this;
        }

        public Builder withAddHints(List<ShipTypeHints> addHints) {
            if (addHints == null) return this;
            skin.addHints = addHints;
            return this;
        }

        public Builder withRemoveWeaponSlots(List<String> removeWeaponSlots) {
            if (removeWeaponSlots == null) return this;
            skin.removeWeaponSlots = removeWeaponSlots;
            return this;
        }

        public Builder withRemoveEngineSlots(List<Integer> removeEngineSlots) {
            if (removeEngineSlots == null) return this;
            skin.removeEngineSlots = removeEngineSlots;
            return this;
        }

        public Builder withRemoveBuiltInMods(Collection<String> removeBuiltInMods) {
            if (removeBuiltInMods == null) {
                skin.removeBuiltInMods = new ArrayList<>();
                return this;
            }
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, HullmodCSVEntry> allHullmodEntries = gameData.getAllHullmodEntries();
            List<HullmodCSVEntry> removeList = new ArrayList<>(removeBuiltInMods.size());
            removeBuiltInMods.forEach(hullmodID -> {
                HullmodCSVEntry hullmodEntry = allHullmodEntries.get(hullmodID);
                removeList.add(hullmodEntry);
            });
            skin.removeBuiltInMods = removeList;
            return this;
        }

        public Builder withRemoveBuiltInWeapons(List<String> removeBuiltInWeapons) {
            if (removeBuiltInWeapons == null) return this;
            skin.removeBuiltInWeapons = removeBuiltInWeapons;
            return this;
        }

        public Builder withBuiltInMods(Collection<String> builtInMods) {
            if (builtInMods == null) {
                skin.builtInMods = new ArrayList<>();
                return this;
            }
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, HullmodCSVEntry> allHullmodEntries = gameData.getAllHullmodEntries();
            List<HullmodCSVEntry> builtInList = new ArrayList<>(builtInMods.size());
            builtInMods.forEach(hullmodID -> {
                HullmodCSVEntry hullmodEntry = allHullmodEntries.get(hullmodID);
                builtInList.add(hullmodEntry);
            });
            skin.builtInMods = builtInList;
            return this;
        }

        /**
         * @param builtInWeapons map where keys are slot IDs and values are weapon IDs.
         */
        public Builder withBuiltInWeapons(Map<String, String> builtInWeapons) {
            if (builtInWeapons == null) return this;
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, WeaponCSVEntry> allWeapons = gameData.getAllWeaponEntries();
            Map<String, WeaponCSVEntry> weapons = new HashMap<>(builtInWeapons.size());

            builtInWeapons.forEach((slotID, weaponID) -> {
                WeaponCSVEntry entry = allWeapons.get(weaponID);
                weapons.put(slotID, entry);
            });

            skin.builtInWeapons = weapons;
            return this;
        }

        public Builder withWeaponSlotChanges(Map<String, WeaponSlot> weaponSlotChanges) {
            if (weaponSlotChanges == null) return this;
            Map<String, WeaponSlotOverride> overrides = new HashMap<>(weaponSlotChanges.size());

            weaponSlotChanges.forEach((slotID, weaponSlot) -> {

                WeaponSlotOverride.WeaponSlotOverrideBuilder overrideBlueprint = WeaponSlotOverride.builder();
                WeaponType type = WeaponType.value(weaponSlot.getType());
                WeaponSize size = WeaponSize.value(weaponSlot.getSize());
                WeaponMount mount = WeaponMount.value(weaponSlot.getMount());
                WeaponSlotOverride override = overrideBlueprint.slotID(slotID)
                        .weaponType(type)
                        .weaponSize(size)
                        .weaponMount(mount)
                        .build();
                overrides.put(slotID, override);
            });

            skin.weaponSlotChanges = overrides;
            return this;
        }

        public Builder withEngineSlotChanges(Map<String, EngineSlot> engineSlotChanges) {
            if (engineSlotChanges == null) return this;

            Map<Integer, EngineDataOverride> overrides = new HashMap<>(engineSlotChanges.size());

            engineSlotChanges.forEach((slotIndex, engineSlot) -> {
                EngineDataOverride.EngineDataOverrideBuilder overrideBlueprint = EngineDataOverride.builder();

                Integer index = Integer.valueOf(slotIndex);
                EngineDataOverride override = overrideBlueprint.index(index)
                        .angle(engineSlot.getAngle())
                        .length(engineSlot.getLength())
                        .width(engineSlot.getWidth())
                        .styleID(engineSlot.getStyle())
                        .build();
                overrides.put(index, override);
            });

            skin.engineSlotChanges = overrides;
            return this;
        }

        public ShipSkin build() {
            ShipSkin ready = this.skin;
            this.skin = new ShipSkin();
            return ready;
        }
    }

    public static ShipSkin createFromSpec(SkinSpecFile skinSpecFile) {
        // This whole class file is obviously an anti-pattern, but I'm just so burned out at this point that I don't really care.
        return new Builder()
                .withSkinFilePath(skinSpecFile.getFilePath())
                .withContainingPackage(skinSpecFile.getContainingPackage())
                .withLoadedSkinSprite(skinSpecFile.getLoadedSkinSprite())
                .withBaseHullId(skinSpecFile.getBaseHullId())
                .withSkinHullId(skinSpecFile.getSkinHullId())
                .withShipSystem(skinSpecFile.getSystemId())
                .withHullName(skinSpecFile.getHullName())
                .withHullDesignation(skinSpecFile.getHullDesignation())
                .withHullStyle(skinSpecFile.getHullStyle())
                .withRestoreToBaseHull(skinSpecFile.isRestoreToBaseHull())
                .withIncompatibleWithBaseHull(skinSpecFile.isIncompatibleWithBaseHull())
                .withFleetPoints(skinSpecFile.getFleetPoints())
                .withOrdnancePoints(skinSpecFile.getOrdnancePoints())
                .withBaseValue(skinSpecFile.getBaseValue())
                .withSuppliesPerMonth(skinSpecFile.getSuppliesPerMonth())
                .withSuppliesToRecover(skinSpecFile.getSuppliesToRecover())
                .withDescriptionId(skinSpecFile.getDescriptionId())
                .withDescriptionPrefix(skinSpecFile.getDescriptionPrefix())
                .withCoversColor(skinSpecFile.getCoversColor())
                .withTags(skinSpecFile.getTags())
                .withTech(skinSpecFile.getTech())
                .withBuiltInWings(skinSpecFile.getBuiltInWings())
                .withFighterBays(skinSpecFile.getFighterBays())
                .withSpriteName(skinSpecFile.getSpriteName())
                .withBaseValueMult(skinSpecFile.getBaseValueMult())
                .withRemoveHints(skinSpecFile.getRemoveHints())
                .withAddHints(skinSpecFile.getAddHints())
                .withRemoveWeaponSlots(skinSpecFile.getRemoveWeaponSlots())
                .withRemoveEngineSlots(skinSpecFile.getRemoveEngineSlots())
                .withRemoveBuiltInMods(skinSpecFile.getRemoveBuiltInMods())
                .withRemoveBuiltInWeapons(skinSpecFile.getRemoveBuiltInWeapons())
                .withBuiltInMods(skinSpecFile.getBuiltInMods())
                .withBuiltInWeapons(skinSpecFile.getBuiltInWeapons())
                .withWeaponSlotChanges(skinSpecFile.getWeaponSlotChanges())
                .withEngineSlotChanges(skinSpecFile.getEngineSlotChanges())
                .build();
    }

}
