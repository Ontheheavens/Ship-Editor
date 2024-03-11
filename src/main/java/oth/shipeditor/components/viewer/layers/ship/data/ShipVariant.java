package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.FireMode;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.HullSize;
import oth.shipeditor.representation.ship.SpecWeaponGroup;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Heavy-footprint runtime variant class; stores full-fledged painters and point indexes for display in viewer.
 * @author Ontheheavens
 * @since 28.08.2023
 */
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyCoupledClass", "OverlyComplexClass"})
@Getter @Setter
public class ShipVariant implements Variant {

    public static final String EMPTY_VARIANT = "Empty variant";

    /**
     * Signifies that the instance is a placeholder variant, not a variant without anything installed;
     * Variants that don't have weapons yet but can install them are normal variants.
     */
    private boolean empty;

    private Path variantFilePath;

    private Path containingPackage;

    /**
     * Can be either ID of base hull or skin hull ID.
     */
    private String shipHullId;

    private String variantId;

    private String displayName = "Custom";

    private List<HullmodCSVEntry> hullMods = new ArrayList<>();

    private List<HullmodCSVEntry> permaMods = new ArrayList<>();

    private List<HullmodCSVEntry> sMods = new ArrayList<>();

    private List<WingCSVEntry> wings = new ArrayList<>();

    private ListOrderedMap<String, InstalledFeature> fittedModules;

    private List<FittedWeaponGroup> weaponGroups = new ArrayList<>();

    private boolean goalVariant;

    private double quality;

    private int fluxCapacitors;

    private int fluxVents;

    private boolean loadedFromFile;

    public ShipVariant() {
        this(true);
    }

    @SuppressWarnings("BooleanParameter")
    public ShipVariant(boolean isEmpty) {
        this.empty = isEmpty;
        this.fittedModules = new ListOrderedMap<>();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static String createUniqueVariantID(ShipLayer shipLayer) {
        String result;
        String shipID = shipLayer.getShipID();

        result = shipID + "_custom";

        int counter = 1;
        while (true) {
            Map<String, ShipVariant> loadedVariants = shipLayer.getLoadedVariants();
            if (!loadedVariants.containsKey(result)) break;
            result = shipID + "_custom_" + String.format("%02d", counter);
            counter++;
        }

        return result;
    }

    public List<InstalledFeature> getFittedModulesList() {
        return fittedModules.valueList();
    }

    private void setFittedModules(Map<String, InstalledFeature> modules) {
        this.fittedModules = new ListOrderedMap<>();
        this.fittedModules.putAll(modules);
    }

    public void removeWeaponGroup(FittedWeaponGroup group) {
        EditDispatch.postWeaponGroupRemoved(this, group);
    }

    public void sortModules(Map<String, InstalledFeature> rearranged) {
        var old = this.getFittedModules();
        EditDispatch.postModulesSorted(this::setFittedModules, old, rearranged);
    }

    public String getFileName() {
        if (variantFilePath == null) return StringValues.EMPTY;
        return variantFilePath.getFileName().toString();
    }

    public void ensureBuiltInsSync(ShipPainter painter) {
        var builtIns = painter.getBuiltInsWithSkin(false, true);

        Map<String, InstalledFeature> fittedWeapons = this.getAllFittedWeapons();
        fittedWeapons.forEach((slotID, feature) -> {
            if (feature.isContainedInBuiltIns() && !builtIns.containsValue(feature)) {
                FittedWeaponGroup parentGroup = feature.getParentGroup();
                ListOrderedMap<String, InstalledFeature> installedWeapons = parentGroup.getWeapons();
                installedWeapons.remove(slotID);
            }
        });

        if (builtIns == null || builtIns.isEmpty()) {
            return;
        }

        builtIns.forEach((slotID, feature) -> {
            FittedWeaponGroup groupWithFit = this.getGroupWithExistingMapping(feature.getSlotID());
            if (groupWithFit != null) {
                var groupWeapons = groupWithFit.getWeapons();
                int index = groupWeapons.indexOf(slotID);
                feature.setParentGroup(groupWithFit);
                groupWeapons.put(index, slotID, feature);
            } else {
                if (weaponGroups.isEmpty() || weaponGroups.get(0) == null) {
                    FittedWeaponGroup newGroup = new FittedWeaponGroup(this,
                            false, FireMode.LINKED);
                    this.weaponGroups.add(newGroup);
                    feature.setParentGroup(newGroup);
                    var groupWeapons = newGroup.getWeapons();
                    groupWeapons.put(slotID, feature);
                } else {
                    FittedWeaponGroup firstGroup = weaponGroups.get(0);
                    var groupWeapons = firstGroup.getWeapons();
                    feature.setParentGroup(firstGroup);
                    groupWeapons.put(slotID, feature);
                }
            }
        });
    }

    public FittedWeaponGroup getGroupWithExistingMapping(String inputSlotID) {
        FittedWeaponGroup result = null;
        for (FittedWeaponGroup group : weaponGroups) {
            var groupWeapons = group.getWeapons();
            var slotIDs = groupWeapons.keyList();
            for (String checkedSlotID : slotIDs) {
                if (Objects.equals(checkedSlotID, inputSlotID)) {
                    result = group;
                    break;
                }
            }
        }
        return result;
    }

    public ShipCSVEntry getEntryFromShipID() {
        var baseHullID = GameDataRepository.getBaseHullID(this.shipHullId);
        return GameDataRepository.retrieveShipCSVEntryByID(baseHullID);
    }

    public int getFittedWingsCount() {
        return this.wings.size();
    }

    public int getTotalUsedOP(ShipLayer parentLayer) {
        int result = 0;

        result += getTotalOPInWings();
        result += getTotalOPInWeapons();
        result += getTotalOPInHullmods(parentLayer);

        result += getFluxVents();
        result += getFluxCapacitors();

        return result;
    }

    public int getTotalOPInWeapons() {
        var allWeapons = this.getAllFittedWeapons();
        int totalOP = 0;

        for (InstalledFeature feature : allWeapons.values()) {
            if (feature.isContainedInBuiltIns()) continue;
            totalOP += feature.getOPCost();
        }
        return totalOP;
    }

    /**
     * @param parentLayer needed to determine hull size, on which hullmod costs are dependent.
     */
    @SuppressWarnings("WeakerAccess")
    public int getTotalOPInHullmods(ShipLayer parentLayer) {
        if (parentLayer == null) return -1;
        ShipHull shipHull = parentLayer.getHull();
        if (shipHull == null) return -1;

        HullSize size = shipHull.getHullSize();
        int result = 0;
        for (HullmodCSVEntry entry : hullMods) {
            result += entry.getOrdnanceCost(size);
        }
        return result;
    }

    public int getTotalOPInWings() {
        int totalOPInWings = 0;
        var wingCSVEntries = this.getWings();
        for (WingCSVEntry wingCSVEntry : wingCSVEntries) {
            totalOPInWings += wingCSVEntry.getOrdnanceCost(null);
        }
        return totalOPInWings;
    }

    public Map<String, InstalledFeature> getAllFittedWeapons() {
        Map<String, InstalledFeature> result = new LinkedHashMap<>();
        for (FittedWeaponGroup weaponGroup : weaponGroups) {
            var weaponsInGroup = weaponGroup.getWeapons();
            result.putAll(weaponsInGroup);
        }
        return result;
    }

    public List<InstalledFeature> getAllFittedWeaponsList() {
        List<InstalledFeature> result = new ArrayList<>();
        for (FittedWeaponGroup weaponGroup : weaponGroups) {
            var weaponsInGroup = weaponGroup.getWeapons();
            result.addAll(weaponsInGroup.valueList());
        }
        return result;
    }

    public void setOpacityForAllFitted(float opacity) {
        List<InstalledFeature> fittedWeaponsList = this.getAllFittedWeaponsList();
        fittedWeaponsList.forEach(weapon -> {
            LayerPainter featurePainter = weapon.getFeaturePainter();
            featurePainter.setSpriteOpacity(opacity);
        });
    }

    public void initialize(VariantFile file) {
        this.setVariantId(file.getVariantId());
        this.setShipHullId(file.getHullId());
        this.setVariantFilePath(file.getVariantFilePath());
        this.setContainingPackage(file.getContainingPackage());
        this.setDisplayName(file.getDisplayName());

        this.setFluxVents(file.getFluxVents());
        this.setFluxCapacitors(file.getFluxCapacitors());

        this.setGoalVariant(file.isGoalVariant());
        this.setQuality(file.getQuality());

        loadedFromFile = true;

        weaponGroups = new ArrayList<>();

        List<SpecWeaponGroup> specWeaponGroups = file.getWeaponGroups();
        for (SpecWeaponGroup weaponGroup : specWeaponGroups) {
            String weaponGroupMode = weaponGroup.getMode();
            FireMode mode = FireMode.valueOf(weaponGroupMode);
            FittedWeaponGroup initialized = new FittedWeaponGroup(this, weaponGroup.isAutofire(), mode);
            var fitted = initialized.getWeapons();
            Map<String, String> specGroupWeapons = weaponGroup.getWeapons();
            for (Map.Entry<String, String> entry : specGroupWeapons.entrySet()) {
                String slotID = entry.getKey();
                String weaponID = entry.getValue();
                WeaponCSVEntry weaponEntry = GameDataRepository.getWeaponByID(weaponID);
                if (weaponEntry == null) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to locate weapon entry for variant, weapon ID: " + weaponID,
                            StringValues.VARIANT_INITIALIZATION_ERROR,
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                WeaponSpecFile specFile = weaponEntry.getSpecFile();
                WeaponPainter weaponPainter = weaponEntry.createPainterFromEntry(null, specFile);
                InstalledFeature feature = InstalledFeature.of(slotID, weaponID, weaponPainter, weaponEntry);
                feature.setParentGroup(initialized);
                fitted.put(slotID, feature);
            }
            weaponGroups.add(initialized);
        }

        var installedModules = file.getModules();
        if (installedModules != null) {
            fittedModules = new ListOrderedMap<>();
            installedModules.forEach((slotID, variantID) -> {
                VariantFile variant = GameDataRepository.getVariantByID(variantID);
                InstalledFeature moduleFeature = GameDataRepository.createModuleFromVariant(slotID, variant);
                fittedModules.put(slotID, moduleFeature);
            });
        }

        var normalMods = file.getHullMods();
        if (normalMods != null) {
            this.hullMods = ShipVariant.constructModsList(normalMods);
        }

        var filePermaMods = file.getPermaMods();
        if (filePermaMods != null) {
            this.permaMods = ShipVariant.constructModsList(filePermaMods);
        }

        var fileSMods = file.getSMods();
        if (fileSMods != null) {
            this.sMods = ShipVariant.constructModsList(fileSMods);
        }

        var fileWings = file.getWings();
        if (fileWings != null) {
            this.wings = new ArrayList<>();
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, WingCSVEntry> allWingEntries = gameData.getAllWingEntries();
            fileWings.forEach(wingID -> {
                WingCSVEntry entry = allWingEntries.get(wingID);
                if (entry != null) {
                    wings.add(entry);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Wing entry not found, skipping in shown variant. ID: " + wingID,
                            StringValues.FILE_LOADING_ERROR,
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private static List<HullmodCSVEntry> constructModsList(Iterable<String> rawList) {
        List<HullmodCSVEntry> result = new ArrayList<>();
        rawList.forEach(hullmodID -> {
            HullmodCSVEntry entry = GameDataRepository.retrieveHullmodCSVEntryByID(hullmodID);
            if (entry != null) {
                result.add(entry);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Hullmod entry not found, skipping in shown variant. ID: " + hullmodID,
                        StringValues.FILE_LOADING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        return result;
    }

    @Override
    public String toString() {
        if (empty) {
            return EMPTY_VARIANT;
        }
        var hullFile = GameDataRepository.retrieveSpecByID(this.getShipHullId());
        if (hullFile != null) {
            String hullName = hullFile.getHullName();
            return this.displayName + " " + hullName;
        }
        return displayName;
    }

}
