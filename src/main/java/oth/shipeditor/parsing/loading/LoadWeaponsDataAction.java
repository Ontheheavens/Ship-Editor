package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WeaponTreeReloadQueued;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.ProjectileSpecFile;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        Map<Path, List<WeaponCSVEntry>> entryListsByPackage = new HashMap<>();

        for (Path folder : modsWithWeaponFolder) {
            Map<String, WeaponCSVEntry> weaponsFromPackage = LoadWeaponsDataAction.walkWeaponsFolder(folder);
            allWeapons.putAll(weaponsFromPackage);
            List<WeaponCSVEntry> entries = new ArrayList<>(weaponsFromPackage.values());
            entryListsByPackage.put(folder, entries);
        }
        gameData.setWeaponsDataLoaded(true);
        gameData.setWeaponEntriesByPackage(entryListsByPackage);

        EventBus.publish(new WeaponTreeReloadQueued());


        Map<String, ProjectileSpecFile> projectiles = LoadWeaponsDataAction.collectProjectiles();
        gameData.setAllProjectiles(projectiles);
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
            mapped.setContainingPackage(folder);
            mappedWeaponSpecs.put(mapped.getId(), mapped);
        }
        log.info("Fetched and mapped {} weapon files.", mappedWeaponSpecs.size());

        Map<String, WeaponCSVEntry> weaponEntries = new HashMap<>();
        for (Map<String, String> row : csvData) {
            String rowId = row.get("id");
            if (rowId != null && !rowId.isEmpty()) {
                WeaponCSVEntry newEntry = new WeaponCSVEntry(row, folder, weaponTablePath);
                WeaponSpecFile matching = mappedWeaponSpecs.get(rowId);
                if (matching != null) {
                    newEntry.setSpecFile(matching);
                    weaponEntries.put(rowId, newEntry);
                } else {
                    log.error("Weapon CSV entry does not have matching spec file, omitting from data repository. " +
                            "ID: {}", rowId);
                }
            }
        }

        return weaponEntries;
    }

    private static Map<String, ProjectileSpecFile> collectProjectiles() {
        String proj = "proj";
        Path projectileFolderTarget = Paths.get("data", StringConstants.WEAPONS, proj);
        Map<Path, File> packagesWithProjectiles = FileUtilities.getFileFromPackages(projectileFolderTarget);
        Collection<Path> projectileFolders = packagesWithProjectiles.keySet();

        Map<String, ProjectileSpecFile> allProjectiles = new HashMap<>();
        for (Path directory : projectileFolders) {
            log.info("Projectile folder found in mod directory: {}", directory);

            List<File> projectileFiles = FileLoading.fetchFilesWithExtension(directory, proj);

            for (File projectileFile : projectileFiles) {
                ProjectileSpecFile mapped = FileLoading.loadProjectileFile(projectileFile);
                mapped.setContainingPackage(directory);
                allProjectiles.put(mapped.getId(), mapped);
            }
        }

        return allProjectiles;
    }

}
