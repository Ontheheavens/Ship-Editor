package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodDataSet;
import oth.shipeditor.communication.events.files.WingDataSet;
import oth.shipeditor.components.datafiles.entities.*;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.weapon.ProjectileSpecFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@SuppressWarnings({"ClassWithTooManyFields", "StaticMethodOnlyUsedInOneClass", "unused"})
@Getter
public class GameDataRepository {

    /**
     * All ship entries by their hull IDs.
     */
    private final Map<String, ShipCSVEntry> allShipEntries;

    /**
     * Holds the same instances as id-entry collection, used for quick repopulating of entry tree with filtering.
     */
    @Setter
    private Map<Path, List<ShipCSVEntry>> shipEntriesByPackage;

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

    @Setter
    private Map<Path, List<WeaponCSVEntry>> weaponEntriesByPackage;

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

    public static ShipCSVEntry retrieveShipCSVEntryByID(String baseHullID) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var shipEntries = dataRepository.getAllShipEntries();
        return shipEntries.get(baseHullID);
    }

    public static ShipSpecFile retrieveSpecByID(String hullID) {
        GameDataRepository dataRepository = SettingsManager.getGameData();
        var allSpecs = dataRepository.getAllSpecEntries();
        return allSpecs.get(hullID);
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
