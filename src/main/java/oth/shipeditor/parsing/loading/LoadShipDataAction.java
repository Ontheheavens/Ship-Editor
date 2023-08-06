package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.communication.events.files.HullTreeCleanupQueued;
import oth.shipeditor.communication.events.files.HullTreeExpansionQueued;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.Variant;
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

        EventBus.publish(new HullTreeCleanupQueued());

        Map<String, SkinSpecFile> allSkins = new HashMap<>();
        for (Path directory : modsWithSkinFolder) {
            log.info("Skin folder found in mod directory: {}", directory);
            Map<String, SkinSpecFile> containedSkins = LoadShipDataAction.walkSkinFolder(directory);
            allSkins.putAll(containedSkins);
        }

        for (Path folder : modsWithShipData) {
            LoadShipDataAction.walkHullFolder(folder.toString(), allSkins);
        }
        EventBus.publish(new HullTreeExpansionQueued());
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setShipDataLoaded(true);

        Map<String, Variant> variants = LoadShipDataAction.collectVariants();
        gameData.setAllVariants(variants);
    }

    private static void walkHullFolder(String folderPath, Map<String, SkinSpecFile> skins) {
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

        EventBus.publish(new HullFolderWalked(csvData, mappedHulls,
                skins, Paths.get(folderPath, "")));
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

    private static Map<String, Variant> collectVariants() {
        Path variantFolderTarget = Paths.get("data", "variants");
        Map<Path, File> packagesWithVariants = FileUtilities.getFileFromPackages(variantFolderTarget);
        Collection<Path> variantFolders = packagesWithVariants.keySet();

        Map<String, Variant> allVariants = new HashMap<>();
        for (Path directory : variantFolders) {
            log.info("Variant folder found in mod directory: {}", directory);

            List<File> variantFiles = FileLoading.fetchFilesWithExtension(directory, StringConstants.VARIANT);

            for (File variantFile : variantFiles) {
                Variant mapped = FileLoading.loadVariantFile(variantFile);
                mapped.setContainingPackage(directory);
                allVariants.put(mapped.getVariantId(), mapped);
            }
        }

        return allVariants;
    }

}
