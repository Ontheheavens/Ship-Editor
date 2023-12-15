package oth.shipeditor.parsing.saving;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.FireMode;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.SpecWeaponGroup;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.utility.Errors;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
@Log4j2
final class SaveVariantAction {

    private SaveVariantAction() {
    }

    static void saveVariant(ShipVariant variant) {
        JFileChooser fileChooser = SaveVariantAction.getSaveVariantFileChooser();

        File currentDirectory = fileChooser.getCurrentDirectory();
        File initial = new File(currentDirectory, variant.getVariantId());
        fileChooser.setSelectedFile(initial);

        VariantFile existing = GameDataRepository.getVariantByID(variant.getVariantId());
        if (existing != null) {
            Path specFilePath = existing.getVariantFilePath();
            File originalPath = specFilePath.toFile();
            if (originalPath.isFile()) {
                fileChooser.setSelectedFile(originalPath);
            }
        }

        int returnVal = fileChooser.showSaveDialog(null);

        File lastVariantDirectory = fileChooser.getCurrentDirectory();
        FileUtilities.setLastVariantDirectory(lastVariantDirectory);
        FileUtilities.setLastGeneralDirectory(lastVariantDirectory);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String extension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            File result = FileUtilities.ensureFileExtension(fileChooser, extension);

            log.info("Commencing variant saving: {}", result);

            ObjectMapper objectMapper = FileUtilities.getConfigured();
            VariantFile toSerialize = SaveVariantAction.rebuildVariantFile(variant);
            try {
                objectMapper.writeValue(result, toSerialize);
            } catch (IOException e) {
                log.error("Variant file saving failed: {}", result.getName());
                JOptionPane.showMessageDialog(null,
                        "Variant file saving failed, exception thrown at: " + result,
                        StringValues.FILE_SAVING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
                Errors.printToStream(e);
            }
        }
    }

    private static VariantFile rebuildVariantFile(ShipVariant shipVariant) {
        VariantFile result = new VariantFile();

        result.setDisplayName(shipVariant.getDisplayName());
        result.setVariantId(shipVariant.getVariantId());
        result.setGoalVariant(shipVariant.isGoalVariant());
        result.setFluxCapacitors(shipVariant.getFluxCapacitors());
        result.setFluxVents(shipVariant.getFluxVents());
        result.setHullId(shipVariant.getShipHullId());

        result.setQuality(shipVariant.getQuality());

        List<HullmodCSVEntry> hullMods = shipVariant.getHullMods();
        result.setHullMods(hullMods.stream().map(HullmodCSVEntry::getHullmodID).collect(Collectors.toList()));
        List<HullmodCSVEntry> permaMods = shipVariant.getPermaMods();
        result.setPermaMods(permaMods.stream().map(HullmodCSVEntry::getHullmodID).collect(Collectors.toList()));
        List<HullmodCSVEntry> sMods = shipVariant.getSMods();
        result.setSMods(sMods.stream().map(HullmodCSVEntry::getHullmodID).collect(Collectors.toList()));

        List<SpecWeaponGroup> specWeaponGroups = SaveVariantAction.recreateSpecWeaponGroups(shipVariant);
        result.setWeaponGroups(specWeaponGroups);

        List<WingCSVEntry> wings = shipVariant.getWings();
        result.setWings(wings.stream().map(WingCSVEntry::getWingID).collect(Collectors.toList()));

        Map<String, String> modules = new LinkedHashMap<>();
        Map<String, InstalledFeature> fittedModules = shipVariant.getFittedModules();
        if (fittedModules != null) {
            fittedModules.forEach((slotID, moduleFeature) -> {
                String moduleHullID = moduleFeature.getFeatureID();
                modules.put(slotID, moduleHullID);
            });
        }
        result.setModules(modules);

        return result;
    }

    private static List<SpecWeaponGroup> recreateSpecWeaponGroups(ShipVariant shipVariant) {
        List<FittedWeaponGroup> weaponGroups = shipVariant.getWeaponGroups();

        List<SpecWeaponGroup> specWeaponGroups = new ArrayList<>();
        weaponGroups.forEach(fittedWeaponGroup -> {
            SpecWeaponGroup specWeaponGroup = new SpecWeaponGroup();
            FireMode mode = fittedWeaponGroup.getMode();
            specWeaponGroup.setMode(mode.toString());
            specWeaponGroup.setAutofire(fittedWeaponGroup.isAutofire());

            Map<String, String> specGroupWeapons = new LinkedHashMap<>();
            ListOrderedMap<String, InstalledFeature> groupWeapons = fittedWeaponGroup.getWeapons();
            groupWeapons.forEach((slotID, feature) -> {
                String weaponID = feature.getFeatureID();
                specGroupWeapons.put(slotID, weaponID);
            });

            specWeaponGroup.setWeapons(specGroupWeapons);
            specWeaponGroups.add(specWeaponGroup);
        });
        return specWeaponGroups;
    }

    private static JFileChooser getSaveVariantFileChooser() {
        FileNameExtensionFilter variantFileFilter = new FileNameExtensionFilter(
                "JSON variant files", StringConstants.VARIANT);

        JFileChooser fileChooser = FileUtilities.getFileChooser(variantFileFilter);

        File directory = FileUtilities.getLastVariantDirectory();
        if (directory != null) {
            fileChooser.setCurrentDirectory(directory);
        }

        return fileChooser;
    }

}
