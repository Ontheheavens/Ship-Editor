package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodDataSet;
import oth.shipeditor.communication.events.files.WingDataSet;
import oth.shipeditor.components.datafiles.entities.*;
import oth.shipeditor.components.datafiles.trees.WeaponFilterPanel;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.ship.*;
import oth.shipeditor.representation.weapon.ProjectileSpecFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods", "StaticMethodOnlyUsedInOneClass", "unused"})
@Getter
public class GameDataRepository {

    /**
     * All ship entries by their hull IDs.
     */
    private final Map<String, ShipCSVEntry> allShipEntries;

    /**
     * Base hull and skin entries by their ship hull IDs. Used when layer needs to be loaded from variant ID.
     */
    private final Map<String, ShipSpecFile> allSpecEntries;

    /**
     * All hullmod entries by their IDs.
     */
    private final Map<String, HullmodCSVEntry> allHullmodEntries;

    /**
     * All shipsystem entries by their IDs.
     */
    private final Map<String, ShipSystemCSVEntry> allShipsystemEntries;

    private final Map<String, WingCSVEntry> allWingEntries;

    private final Map<String, WeaponCSVEntry> allWeaponEntries;

    /**
     * Holds the same instances as id-entry collection, used for quick repopulating of entry tree with filtering.
     */
    private Map<Path, List<ShipCSVEntry>> shipEntriesByPackage;

    private Map<Path, List<WeaponCSVEntry>> weaponEntriesByPackage;

    private Map<Path, List<HullmodCSVEntry>> hullmodEntriesByPackage;

    private Map<Path, List<ShipSystemCSVEntry>> shipSystemEntriesByPackage;

    private Map<Path, List<WingCSVEntry>> wingEntriesByPackage;


    /**
     * Hull styles by their IDs (field names in JSON).
     */
    @Setter
    private Map<String, HullStyle> allHullStyles;

    /**
     * Engine styles by their IDs (field names in JSON).
     */
    @Setter
    private Map<String, EngineStyle> allEngineStyles;

    /**
     * All variant files by variant IDs.
     */
    @Setter
    private Map<String, VariantFile> allVariants;

    /**
     * All projectile files by variant IDs.
     */
    @Setter
    private Map<String, ProjectileSpecFile> allProjectiles;

    @Setter
    private boolean shipDataLoaded;

    private boolean hullmodDataLoaded;

    @Setter
    private boolean shipsystemDataLoaded;

    private boolean wingDataLoaded;

    @Setter
    private boolean weaponsDataLoaded;

    public GameDataRepository() {
        this.allSpecEntries = new HashMap<>();
        this.allShipEntries = new HashMap<>();
        this.allHullmodEntries = new HashMap<>();
        this.allShipsystemEntries = new HashMap<>();
        this.allWingEntries = new HashMap<>();
        this.allWeaponEntries = new HashMap<>();
    }


    public void setShipEntriesByPackage(Map<Path, List<ShipCSVEntry>> shipEntries) {
        this.shipEntriesByPackage = shipEntries;
        SettingsManager.announcePackages(shipEntries);
    }

    public void setWeaponEntriesByPackage(Map<Path, List<WeaponCSVEntry>> weaponEntries) {
        this.weaponEntriesByPackage = weaponEntries;
        Map<Path, Boolean> filterEntries = new LinkedHashMap<>();
        weaponEntries.forEach((path, weaponCSVEntries) -> filterEntries.put(path, true));

        SettingsManager.announcePackages(weaponEntries);
        WeaponFilterPanel.setPackageFilters(filterEntries);
    }

    public void setHullmodEntriesByPackage(Map<Path, List<HullmodCSVEntry>> hullmodEntries) {
        this.hullmodEntriesByPackage = hullmodEntries;
        SettingsManager.announcePackages(hullmodEntries);
    }

    public void setShipSystemEntriesByPackage(Map<Path, List<ShipSystemCSVEntry>> shipSystemEntries) {
        this.shipSystemEntriesByPackage = shipSystemEntries;
        SettingsManager.announcePackages(shipSystemEntries);
    }

    public void setWingEntriesByPackage(Map<Path, List<WingCSVEntry>> wingEntries) {
        this.wingEntriesByPackage = wingEntries;
        SettingsManager.announcePackages(wingEntries);
    }

    public static ShipCSVEntry retrieveShipCSVEntryByID(String baseHullID) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var shipEntries = dataRepository.getAllShipEntries();
        return shipEntries.get(baseHullID);
    }

    public static HullmodCSVEntry retrieveHullmodCSVEntryByID(String hullmodID) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var hullmodEntries = dataRepository.getAllHullmodEntries();
        return hullmodEntries.get(hullmodID);
    }

    public static WeaponCSVEntry retrieveWeaponCSVEntryByID(String weaponID) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var weaponEntries = dataRepository.getAllWeaponEntries();
        return weaponEntries.get(weaponID);
    }

    public static ShipSpecFile retrieveSpecByID(String hullID) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var allSpecs = dataRepository.getAllSpecEntries();
        return allSpecs.get(hullID);
    }

    /**
     * @param shipHullID ship ID, whether base or skin.
     * @return base hull ID.
     */
    public static String getBaseHullID(String shipHullID) {
        ShipSpecFile specFile = GameDataRepository.retrieveSpecByID(shipHullID);
        if (specFile == null) return null;
        String baseHullId;
        if (specFile instanceof SkinSpecFile checkedSkin) {
            baseHullId = checkedSkin.getBaseHullId();
        } else {
            baseHullId = specFile.getHullId();
        }
        return baseHullId;
    }

    public static ShipLayer createLayerFromVariant(Variant variant) {
        String shipHullId = variant.getShipHullId();
        ShipSpecFile specFile = GameDataRepository.retrieveSpecByID(shipHullId);
        String baseHullId;
        SkinSpecFile skinSpec = null;
        if (specFile instanceof SkinSpecFile checkedSkin) {
            baseHullId = checkedSkin.getBaseHullId();
            skinSpec = checkedSkin;
        } else {
            baseHullId = specFile.getHullId();
        }
        ShipCSVEntry csvEntry = GameDataRepository.retrieveShipCSVEntryByID(baseHullId);
        ShipLayer shipLayer = csvEntry.loadLayerFromEntry();
        ShipPainter shipPainter = shipLayer.getPainter();

        if (skinSpec != null) {
            for (ShipSkin skin : shipLayer.getSkins()) {
                if (skin == null || skin.isBase()) continue;
                String skinHullId = skin.getSkinHullId();
                if (skinHullId.equals(skinSpec.getSkinHullId())) {
                    shipPainter.setActiveSpec(ActiveShipSpec.SKIN, skin);
                }
            }
        }

        shipPainter.selectVariant(variant);

        return shipLayer;
    }

    public static InstalledFeature createModuleFromVariant(String slotID, Variant variant) {
        String shipHullId = variant.getShipHullId();
        ShipSpecFile specFile = GameDataRepository.retrieveSpecByID(shipHullId);
        String baseHullId;
        SkinSpecFile skinSpec = null;
        if (specFile instanceof SkinSpecFile checkedSkin) {
            baseHullId = checkedSkin.getBaseHullId();
            skinSpec = checkedSkin;
        } else {
            baseHullId = specFile.getHullId();
        }
        ShipCSVEntry csvEntry = GameDataRepository.retrieveShipCSVEntryByID(baseHullId);
        ShipPainter modulePainter = csvEntry.createPainterFromEntry(null);

        if (skinSpec != null) {
            ShipSkin shipSkin = ShipSkin.createFromSpec(skinSpec);
            modulePainter.setActiveSpec(ActiveShipSpec.SKIN, shipSkin);
        }

        modulePainter.selectVariant(variant);
        return InstalledFeature.of(slotID, variant.getVariantId(), modulePainter, csvEntry);
    }

    public static void putSpec(ShipSpecFile specFile) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var allSpecs = dataRepository.getAllSpecEntries();
        allSpecs.put(specFile.getHullId(), specFile);
    }

    public void setHullmodDataLoaded(boolean hullmodsLoaded) {
        this.hullmodDataLoaded = hullmodsLoaded;
        EventBus.publish(new HullmodDataSet());
    }

    public void setWingDataLoaded(boolean wingsLoaded) {
        this.wingDataLoaded = wingsLoaded;
        EventBus.publish(new WingDataSet());
    }

    public static HullStyle fetchStyleByID(String styleID) {
        var dataRepository = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = dataRepository.getAllHullStyles();
        HullStyle style = null;
        if (allHullStyles != null) {
            style = allHullStyles.get(styleID);
        }
        return style;
    }

    public static VariantFile getVariantByID(String variantID) {
        var dataRepository = SettingsManager.getGameData();
        return dataRepository.allVariants.get(variantID);
    }

    public static ProjectileSpecFile getProjectileByID(String projectileID) {
        var dataRepository = SettingsManager.getGameData();
        return dataRepository.allProjectiles.get(projectileID);
    }

    public static WeaponCSVEntry getWeaponByID(String weaponID) {
        var dataRepository = SettingsManager.getGameData();
        return dataRepository.allWeaponEntries.get(weaponID);
    }

    public static Map<String, VariantFile> getMatchingForHullID(String shipHullID) {
        var dataRepository = SettingsManager.getGameData();
        var allVariants = dataRepository.getAllVariants();
        Map<String, VariantFile> result = new HashMap<>();
        for (Map.Entry<String, VariantFile> variantFileEntry : allVariants.entrySet()) {
            VariantFile variantFile = variantFileEntry.getValue();
            String variantHullId = variantFile.getHullId();
            if (variantHullId.equals(shipHullID)) {
                result.put(variantFileEntry.getKey(), variantFileEntry.getValue());
            }
        }
        return result;
    }

}
