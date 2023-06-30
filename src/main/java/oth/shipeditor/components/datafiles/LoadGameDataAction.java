package oth.shipeditor.components.datafiles;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.communication.events.files.HullTreeExpansionQueued;
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

        List<Path> modsWithShipData = new ArrayList<>();

        try (Stream<Path> childDirectories = Files.list(Paths.get(modFolderPath))) {
            modsWithShipData = childDirectories
                    .filter(Files::isDirectory)
                    .filter(childDir -> Files.exists(childDir.resolve(targetFile)))
                    .collect(Collectors.toList());
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

        for (String folder : eligibleFolders) {
            LoadGameDataAction.walkHullFolder(folder);
        }

        EventBus.publish(new HullTreeExpansionQueued());
        this.setEnabled(false);
    }

    private static void walkHullFolder(String folderPath) {
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
            Hull mapped = oth.shipeditor.menubar.Files.loadHullFile(hullFile);
            mappedHulls.put(hullFile.getName(), mapped);
        }
        log.info("Fetched and mapped {} hull files.", mappedHulls.size());

        Map<String, Skin> skins = null;
        Path skinDataPath = Paths.get(folderPath, "data", HULLS, "skins");
        if (Files.exists(skinDataPath) && Files.isDirectory(skinDataPath)) {
            skins = LoadGameDataAction.walkSkinFolder(skinDataPath);
            for (Map.Entry<String, Skin> entry : skins.entrySet()) {
                log.info("Skin path: {}", entry.getKey());
            }
        }

//        EventBus.publish(new HullFolderWalked(csvData, mappedHulls, Paths.get(folderPath, "")));
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
        for (File hullFile : skinFiles) {
            Skin mapped = oth.shipeditor.menubar.Files.loadSkinFile(hullFile);
            mappedSkins.put(hullFile.getName(), mapped);
        }
        log.info("Fetched and mapped {} skin files.", mappedSkins.size());
        return mappedSkins;
    }

    private static List<Map<String, String>> parseShipDataCSV(Path shipDataPath) {
        CsvMapper csvMapper = new CsvMapper();

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
