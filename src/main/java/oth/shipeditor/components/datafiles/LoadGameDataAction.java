package oth.shipeditor.components.datafiles;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.communication.events.files.HullTreeCleanupQueued;
import oth.shipeditor.communication.events.files.HullTreeExpansionQueued;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
public
class LoadGameDataAction extends AbstractAction {

    private static final String HULLS = "hulls";
    private static final String SHIP_DATA_CSV = "ship_data.csv";

    @Override
    public void actionPerformed(ActionEvent e) {
        Settings settings = SettingsManager.getSettings();
        Collection<String> eligibleFolders = new ArrayList<>();
        eligibleFolders.add(settings.getCoreFolderPath());

        String modFolderPath = settings.getModFolderPath();
        Path targetFile = Paths.get("data", HULLS, SHIP_DATA_CSV);

        Collection<Path> modsWithShipData = new ArrayList<>();
        Collection<Path> modsWithSkinFolder = new ArrayList<>();
        modsWithSkinFolder.add(Paths.get(settings.getCoreFolderPath()));

        try (Stream<Path> childDirectories = Files.list(Paths.get(modFolderPath))) {
            childDirectories
                    .filter(Files::isDirectory)
                    .forEach(childDir -> {
                        Path targetFilePath = childDir.resolve(targetFile);
                        if (Files.exists(targetFilePath)) {
                            modsWithShipData.add(childDir);
                        }

                        Path skinDataPath = Paths.get(childDir.toString(), "data", HULLS, "skins");
                        Path skinsSubfolder = childDir.resolve(skinDataPath);
                        if (Files.exists(skinsSubfolder) && Files.isDirectory(skinsSubfolder)) {
                            modsWithSkinFolder.add(childDir);
                        }
                    });
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (modsWithShipData.isEmpty()) {
            log.info("Ship data file not found in any mod directory.");
        } else {
            for (Path directory : modsWithShipData) {
                log.info("Ship data file found in mod directory: {}", directory);
                eligibleFolders.add(directory.toString());
            }
        }

        EventBus.publish(new HullTreeCleanupQueued());

        Map<String, Skin> allSkins = new HashMap<>();
        for (Path directory : modsWithSkinFolder) {
            log.info("Skin folder found in mod directory: {}", directory);
            Map<String, Skin> containedSkins = LoadGameDataAction.walkSkinFolder(directory);
            allSkins.putAll(containedSkins);
        }

        for (String folder : eligibleFolders) {
            LoadGameDataAction.walkHullFolder(folder, allSkins);
        }

        EventBus.publish(new HullTreeExpansionQueued());
    }

    private static void walkHullFolder(String folderPath, Map<String, Skin> skins) {
        Path shipDataPath = Paths.get(folderPath, "data", HULLS, SHIP_DATA_CSV);

        log.info("Parsing ship CSV data at: {}..", shipDataPath);
        List<Map<String, String>> csvData = LoadGameDataAction.parseShipDataCSV(shipDataPath);
        log.info("Ship CSV data at {} retrieved successfully.", shipDataPath);

        List<File> shipFiles = new ArrayList<>();
        log.info("Bulk fetching hull files at: {}...", folderPath);
        try (Stream<Path> pathStream = Files.walk(shipDataPath.getParent())) {
            pathStream.filter(path -> {
                        String toString = path.getFileName().toString();
                        return toString.endsWith(".ship");
                    })
                    .map(Path::toFile)
                    .forEach(shipFiles::add);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        Map<String, Hull> mappedHulls = new HashMap<>();
        for (File hullFile : shipFiles) {
            Hull mapped = FileUtilities.loadHullFile(hullFile);
            mappedHulls.put(hullFile.getName(), mapped);
        }
        log.info("Fetched and mapped {} hull files.", mappedHulls.size());

        EventBus.publish(new HullFolderWalked(csvData, mappedHulls,
                skins, Paths.get(folderPath, "")));
    }

    private static Map<String, Skin> walkSkinFolder(Path skinFolder) {
        List<File> skinFiles = new ArrayList<>();
        log.info("Bulk fetching skin files at: {}...", skinFolder);
        try (Stream<Path> pathStream = Files.walk(skinFolder)) {
            pathStream.filter(path -> {
                        String toString = path.getFileName().toString();
                        return toString.endsWith(".skin");
                    })
                    .map(Path::toFile)
                    .forEach(skinFiles::add);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        Map<String, Skin> mappedSkins = new HashMap<>();
        for (File skinFile : skinFiles) {
            Skin mapped = FileUtilities.loadSkinFile(skinFile);
            mapped.setContainingPackage(skinFolder);
            mappedSkins.put(skinFile.getName(), mapped);
        }
        log.info("Fetched and mapped {} skin files.", mappedSkins.size());
        return mappedSkins;
    }

    private static List<Map<String, String>> parseShipDataCSV(Path shipDataPath) {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.enable(CsvParser.Feature.ALLOW_COMMENTS);

        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        File csvFile = shipDataPath.toFile();
        List<Map<String, String>> csvData = new ArrayList<>();
        try (MappingIterator<Map<String, String>> iterator = csvMapper.readerFor(Map.class)
                .with(csvSchema)
                .readValues(csvFile)) {
            while (iterator.hasNext()) {
                Map<String, String> row = iterator.next();
                String id = row.get(StringConstants.ID);
                if (!id.isEmpty()) {
                    // We are skipping a row if ship ID is missing.
                    csvData.add(row);
                }
            }
        } catch (IOException exception) {
            log.error("Ship data CSV loading failed!");
            exception.printStackTrace();
        }
        return csvData;
    }

}
