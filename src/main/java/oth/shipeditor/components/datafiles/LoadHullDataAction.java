package oth.shipeditor.components.datafiles;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.utility.StringConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
class LoadHullDataAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        Settings settings = SettingsManager.getSettings();
        String folderPath = "";
        if (command.equals(oth.shipeditor.menubar.Files.STARSECTOR_CORE)) {
            folderPath = settings.getCoreFolderPath();
        }
        if (folderPath.isEmpty()) return;
        Path shipDataPath = Paths.get(folderPath, "data", "hulls", "ship_data.csv");

        log.info("Parsing ship CSV data at: {}..", shipDataPath);
        List<Map<String, String>> csvData = LoadHullDataAction.parseShipDataCSV(shipDataPath);
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

        EventBus.publish(new HullFolderWalked(csvData, mappedHulls, Paths.get(folderPath, "")));
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
