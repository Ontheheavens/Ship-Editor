package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullTreeEntryCleared;
import oth.shipeditor.communication.events.files.HullTreeReloadQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.*;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
public
class LoadShipDataAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        Path hullTableTarget = Paths.get("data", StringConstants.HULLS, StringConstants.SHIP_DATA_CSV);
        Map<Path, File> hullsPackages = FileUtilities.getFileFromPackages(hullTableTarget);
        Collection<Path> modsWithShipData = hullsPackages.keySet();

        Path skinFolderTarget = Paths.get("data", StringConstants.HULLS, "skins");
        Map<Path, File> skinsPackages = FileUtilities.getFileFromPackages(skinFolderTarget);
        Collection<Path> modsWithSkinFolder = skinsPackages.keySet();

        Map<String, SkinSpecFile> allSkins = new HashMap<>();
        for (Path directory : modsWithSkinFolder) {
            log.info("Skin folder found in mod directory: {}", directory);
            Map<String, SkinSpecFile> containedSkins = LoadShipDataAction.walkSkinFolder(directory);
            allSkins.putAll(containedSkins);
        }

        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, ShipCSVEntry> allShipEntries = gameData.getAllShipEntries();
        allShipEntries.clear();
        Map<Path, List<ShipCSVEntry>> allEntriesByPackage = new HashMap<>();

        for (Path folder : modsWithShipData) {
            Pair<Path, List<ShipCSVEntry>> packageShipData = LoadShipDataAction.walkHullFolder(folder.toString(), allSkins);
            allEntriesByPackage.put(packageShipData.getFirst(), packageShipData.getSecond());
        }

        gameData.setShipEntriesByPackage(allEntriesByPackage);
        gameData.setShipDataLoaded(true);
        EventBus.publish(new HullTreeEntryCleared());
        EventBus.publish(new HullTreeReloadQueued());

        Map<String, VariantFile> variants = LoadShipDataAction.collectVariants();
        gameData.setAllVariants(variants);
    }

    private static Pair<Path, List<ShipCSVEntry>> walkHullFolder(String folderPath, Map<String, SkinSpecFile> skins) {
        Path shipTablePath = Paths.get(folderPath, "data", StringConstants.HULLS, StringConstants.SHIP_DATA_CSV);

        log.info("Parsing ship CSV data at: {}..", shipTablePath);
        List<Map<String, String>> csvData = FileLoading.parseCSVTable(shipTablePath);
        log.info("Ship CSV data at {} retrieved successfully.", shipTablePath);

        log.info("Bulk fetching hull files at: {}...", folderPath);
        List<File> shipFiles = FileLoading.fetchFilesWithExtension(shipTablePath.getParent(), "ship");
        Map<String, HullSpecFile> mappedHulls = new HashMap<>();

        for (File hullFile : shipFiles) {
            HullSpecFile mapped = FileLoading.loadHullFile(hullFile);
            mapped.setTableFilePath(shipTablePath);
            mappedHulls.put(hullFile.getName(), mapped);
        }
        log.info("Fetched and mapped {} hull files.", mappedHulls.size());

        Path packagePath = Paths.get(folderPath, "");
        List<ShipCSVEntry> entriesFromPackage = new ArrayList<>();

        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, ShipCSVEntry> allShipEntries = gameData.getAllShipEntries();

        for (Map<String, String> row : csvData) {
            Map.Entry<HullSpecFile, Map<String, SkinSpecFile>> hullWithSkins = null;
            String fileName = "";
            String rowId = row.get("id");
            for (Map.Entry<String, HullSpecFile> entry : mappedHulls.entrySet()) {
                HullSpecFile shipFile = entry.getValue();
                String hullId = shipFile.getHullId();
                if (hullId.equals(rowId)) {
                    fileName = entry.getKey();
                    Map<String, SkinSpecFile> skinsOfHull = LoadShipDataAction.fetchSkinsByHull(shipFile, skins);
                    hullWithSkins = new AbstractMap.SimpleEntry<>(shipFile, skinsOfHull);
                }
            }
            if (hullWithSkins != null && !fileName.isEmpty()) {
                ShipCSVEntry newEntry = new ShipCSVEntry(row, hullWithSkins, packagePath, fileName);
                entriesFromPackage.add(newEntry);
                allShipEntries.put(rowId, newEntry);
            }
        }

        return new Pair<>(packagePath, entriesFromPackage);
    }

    private static Map<String, SkinSpecFile> fetchSkinsByHull(ShipSpecFile hullSpecFile, Map<String, SkinSpecFile> skins) {
        if (skins == null) return null;
        String hullId = hullSpecFile.getHullId();
        Map<String, SkinSpecFile> associated = new HashMap<>();
        for (Map.Entry<String, SkinSpecFile> skin : skins.entrySet()) {
            SkinSpecFile value = skin.getValue();
            if (Objects.equals(value.getBaseHullId(), hullId)) {
                associated.put(skin.getKey(), skin.getValue());
            }
        }
        if (!associated.isEmpty()) {
            return associated;
        }
        return null;
    }

    private static Map<String, SkinSpecFile> walkSkinFolder(Path skinFolder) {
        log.info("Bulk fetching skin files at: {}...", skinFolder);
        List<File> skinFiles = FileLoading.fetchFilesWithExtension(skinFolder, "skin");
        Map<String, SkinSpecFile> mappedSkins = new HashMap<>();
        for (File skinFile : skinFiles) {
            SkinSpecFile mapped = FileLoading.loadSkinFile(skinFile);
            mapped.setContainingPackage(skinFolder);
            mappedSkins.put(skinFile.getName(), mapped);
        }
        log.info("Fetched and mapped {} skin files.", mappedSkins.size());
        return mappedSkins;
    }

    private static Map<String, VariantFile> collectVariants() {
        Path variantFolderTarget = Paths.get("data", "variants");
        Map<Path, File> packagesWithVariants = FileUtilities.getFileFromPackages(variantFolderTarget);
        Collection<Path> variantFolders = packagesWithVariants.keySet();

        Map<String, VariantFile> allVariants = new HashMap<>();
        for (Path directory : variantFolders) {
            log.info("Variant folder found in mod directory: {}", directory);

            List<File> variantFiles = FileLoading.fetchFilesWithExtension(directory, StringConstants.VARIANT);

            for (File variantFile : variantFiles) {
                VariantFile mapped = FileLoading.loadVariantFile(variantFile);
                mapped.setContainingPackage(directory);
                allVariants.put(mapped.getVariantId(), mapped);
            }
        }

        return allVariants;
    }

}
