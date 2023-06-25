package oth.shipeditor.components.datafiles;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.ShipCSVOpened;
import oth.shipeditor.menubar.Files;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
class LoadShipDataAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        Settings settings = SettingsManager.getSettings();
        String folderPath = "";
        if (command.equals(Files.STARSECTOR_CORE)) {
            folderPath = settings.getCoreFolderPath();
        }
        if (folderPath.isEmpty()) return;
        Path shipDataPath = Paths.get(folderPath, "data", "hulls", "ship_data.csv");
        log.info("CSV File Path: {}", shipDataPath);
        List<Map<String, String>> csvData = LoadShipDataAction.parseShipDataCSV(shipDataPath);
        EventBus.publish(new ShipCSVOpened(csvData, command));
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
                String id = row.get("id");
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
