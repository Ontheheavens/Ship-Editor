package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.*;
import oth.shipeditor.components.viewer.entities.engine.EngineDataOverride;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.*;
import oth.shipeditor.representation.ship.EngineSlot;
import oth.shipeditor.representation.ship.HullStyle;
import oth.shipeditor.representation.ship.ShipTypeHints;
import oth.shipeditor.representation.ship.SkinSpecFile;
import oth.shipeditor.representation.weapon.*;
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
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyComplexClass"})
@Log4j2
@Getter @Setter
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

    private Boolean restoreToBaseHull;

    private Boolean incompatibleWithBaseHull;

    private Integer fleetPoints;

    private Integer ordnancePoints;

    private Integer baseValue;

    private Double suppliesPerMonth;

    private Double suppliesToRecover;

    private String descriptionId;

    private String descriptionPrefix;

    private Color coversColor;

    private List<String> tags = new ArrayList<>();

    private String tech;

    private List<WingCSVEntry> builtInWings = new ArrayList<>();

    private Integer fighterBays;

    private String spriteName;

    private Double baseValueMult;

    private List<ShipTypeHints> removeHints = new ArrayList<>();

    private List<ShipTypeHints> addHints = new ArrayList<>();

    private List<String> removeWeaponSlots;

    private List<Integer> removeEngineSlots;

    private List<HullmodCSVEntry> removeBuiltInMods;

    private List<WingCSVEntry> removeBuiltInWings;

    private List<String> removeBuiltInWeapons;

    private List<HullmodCSVEntry> builtInMods;

    private Map<String, WeaponCSVEntry> builtInWeapons;

    /**
     * For runtime usage in viewer.
     */
    private Map<String, InstalledFeature> initializedBuiltIns;

    private Map<String, WeaponSlotOverride> weaponSlotChanges;

    private Map<Integer, EngineDataOverride> engineSlotChanges;

    /**
     * Needs to be called after every change in CSV-type built-ins map.
     * For example, if built-in entry is added or removed, initialized list needs to be invalidated for refresh.
     */
    public void invalidateBuiltIns() {
        log.info("Invalidating built-ins of {}: cleaning up features.",
                "#" + this.hashCode() + " [" + this + "]");
        initializedBuiltIns.forEach((s, feature) -> feature.cleanupForRemoval());
        initializedBuiltIns = null;
    }

    public void setBuiltInWeapons(Map<String, WeaponCSVEntry> entryMap) {
        this.builtInWeapons = entryMap;
        this.invalidateBuiltIns();
    }

    public Map<String, InstalledFeature> getInitializedBuiltIns() {
        if (initializedBuiltIns == null) {
            initializedBuiltIns = new LinkedHashMap<>();

            builtInWeapons.forEach((slotID, weaponEntry) -> {
                WeaponSpecFile specFile = weaponEntry.getSpecFile();
                WeaponPainter weaponPainter = weaponEntry.createPainterFromEntry(null, specFile);
                InstalledFeature feature = InstalledFeature.of(slotID, weaponEntry.getWeaponID(),
                        weaponPainter, weaponEntry);
                feature.setContainedInBuiltIns(true);
                initializedBuiltIns.put(slotID, feature);
            });
        }
        return initializedBuiltIns;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Map<String, WeaponCSVEntry> reconstructAsEntries(Map<String, InstalledFeature> initialized) {
        Map<String, WeaponCSVEntry> result = new LinkedHashMap<>();
        initialized.forEach((slotID, feature) -> {
            WeaponCSVEntry entry = (WeaponCSVEntry) feature.getDataEntry();
            result.put(slotID, entry);
        });
        return result;
    }

    public List<ShipTypeHints> getHintsModifiedBySkin() {
        ShipCSVEntry dataEntry = GameDataRepository.retrieveShipCSVEntryByID(this.baseHullId);
        if (dataEntry == null) return null;
        var fromBaseHull = dataEntry.getBaseHullHints();
        var toRemove = this.getRemoveHints();
        if (toRemove != null && !toRemove.isEmpty()) {
            fromBaseHull.removeAll(toRemove);
        }

        var toAdd = this.getAddHints();
        if (toAdd != null && !toAdd.isEmpty()) {
            fromBaseHull.addAll(toAdd);
        }
        return fromBaseHull;
    }

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
            "unused", "MethodParameterNamingConvention", "WeakerAccess"})
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

        public Builder withRestoreToBaseHull(Boolean restoreToBaseHull) {
            skin.restoreToBaseHull = restoreToBaseHull;
            return this;
        }

        public Builder withIncompatibleWithBaseHull(Boolean incompatibleWithBaseHull) {
            skin.incompatibleWithBaseHull = incompatibleWithBaseHull;
            return this;
        }

        public Builder withFleetPoints(Integer fleetPoints) {
            skin.fleetPoints = fleetPoints;
            return this;
        }

        public Builder withOrdnancePoints(Integer ordnancePoints) {
            skin.ordnancePoints = ordnancePoints;
            return this;
        }

        public Builder withBaseValue(Integer baseValue) {
            skin.baseValue = baseValue;
            return this;
        }

        public Builder withSuppliesPerMonth(Double suppliesPerMonth) {
            skin.suppliesPerMonth = suppliesPerMonth;
            return this;
        }

        public Builder withSuppliesToRecover(Double suppliesToRecover) {
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

        public Builder withFighterBays(Integer fighterBays) {
            skin.fighterBays = fighterBays;
            return this;
        }

        public Builder withSpriteName(String spriteName) {
            skin.spriteName = spriteName;
            return this;
        }

        public Builder withBaseValueMult(Double baseValueMult) {
            skin.baseValueMult = baseValueMult;
            return this;
        }

        public Builder withRemoveHints(List<ShipTypeHints> removeHints) {
            if (removeHints == null) {
                skin.removeHints = new ArrayList<>();
                return this;
            }
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

        public Builder withRemoveBuiltInWings(Collection<String> removeBuiltInWings) {
            if (removeBuiltInWings == null) {
                skin.removeBuiltInWings = new ArrayList<>();
                return this;
            }
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, WingCSVEntry> allWingEntries = gameData.getAllWingEntries();
            List<WingCSVEntry> removeList = new ArrayList<>(removeBuiltInWings.size());
            removeBuiltInWings.forEach(wingID -> {
                WingCSVEntry wingEntry = allWingEntries.get(wingID);
                removeList.add(wingEntry);
            });
            skin.removeBuiltInWings = removeList;
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
            if (builtInWeapons == null) {
                skin.builtInWeapons = new LinkedHashMap<>();
                return this;
            }
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, WeaponCSVEntry> allWeapons = gameData.getAllWeaponEntries();
            Map<String, WeaponCSVEntry> weapons = new LinkedHashMap<>(builtInWeapons.size());

            builtInWeapons.forEach((slotID, weaponID) -> {
                WeaponCSVEntry entry = allWeapons.get(weaponID);
                weapons.put(slotID, entry);
            });

            skin.builtInWeapons = weapons;
            return this;
        }

        public Builder withWeaponSlotChanges(Map<String, WeaponSlot> weaponSlotChanges) {
            if (weaponSlotChanges == null) return this;
            Map<String, WeaponSlotOverride> overrides = new LinkedHashMap<>(weaponSlotChanges.size());

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

            Map<Integer, EngineDataOverride> overrides = new LinkedHashMap<>(engineSlotChanges.size());

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
                .withRestoreToBaseHull(skinSpecFile.getRestoreToBaseHull())
                .withIncompatibleWithBaseHull(skinSpecFile.getIncompatibleWithBaseHull())
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
                .withRemoveBuiltInWings(skinSpecFile.getRemoveBuiltInWings())
                .withRemoveBuiltInWeapons(skinSpecFile.getRemoveBuiltInWeapons())
                .withBuiltInMods(skinSpecFile.getBuiltInMods())
                .withBuiltInWeapons(skinSpecFile.getBuiltInWeapons())
                .withWeaponSlotChanges(skinSpecFile.getWeaponSlotChanges())
                .withEngineSlotChanges(skinSpecFile.getEngineSlotChanges())
                .build();
    }

}
