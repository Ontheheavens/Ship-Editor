package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WeaponDataLoaded;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@Log4j2
public class LoadWeaponsDataAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        Path weaponsFolderTarget = Paths.get("data", StringConstants.WEAPONS);
        Map<Path, File> weaponsPackages = FileUtilities.getFileFromPackages(weaponsFolderTarget);
        Collection<Path> modsWithWeaponFolder = weaponsPackages.keySet();

        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, WeaponCSVEntry> allWeapons = gameData.getAllWeaponEntries();

        Map<Path, Map<String, WeaponCSVEntry>> weaponsByPackage = new HashMap<>();
        for (Path folder : modsWithWeaponFolder) {
            Map<String, WeaponCSVEntry> weaponsFromPackage = LoadWeaponsDataAction.walkWeaponsFolder(folder);
            allWeapons.putAll(weaponsFromPackage);
            weaponsByPackage.put(folder, weaponsFromPackage);
        }

        EventBus.publish(new WeaponDataLoaded(weaponsByPackage));
        gameData.setWeaponsDataLoaded(true);
    }

    private static Map<String, WeaponCSVEntry> walkWeaponsFolder(Path folder) {
        Path weaponTablePath = Paths.get(folder.toString(), "data", StringConstants.WEAPONS, "weapon_data.csv");

        log.info("Parsing weapon CSV data at: {}..", weaponTablePath);
        List<Map<String, String>> csvData = FileLoading.parseCSVTable(weaponTablePath);
        log.info("Weapon CSV data at {} retrieved successfully.", weaponTablePath);

        List<File> weaponFiles = FileLoading.fetchFilesWithExtension(weaponTablePath.getParent(), "wpn");
        Map<String, WeaponSpecFile> mappedWeaponSpecs = new HashMap<>();

        for (File weaponFile : weaponFiles) {
            WeaponSpecFile mapped = FileLoading.loadWeaponFile(weaponFile);
            mapped.setTableFilePath(weaponTablePath);
            mappedWeaponSpecs.put(mapped.getId(), mapped);
        }
        log.info("Fetched and mapped {} weapon files.", mappedWeaponSpecs.size());

        Map<String, WeaponCSVEntry> weaponEntries = new HashMap<>();
        for (Map<String, String> row : csvData) {
            String rowId = row.get("id");
            if (rowId != null && !rowId.isEmpty()) {
                WeaponCSVEntry newEntry = new WeaponCSVEntry(row, folder, weaponTablePath);
                WeaponSpecFile matching = mappedWeaponSpecs.get(rowId);
                newEntry.setSpecFile(matching);
                weaponEntries.put(rowId, newEntry);
            }
        }

        return weaponEntries;
    }

}
