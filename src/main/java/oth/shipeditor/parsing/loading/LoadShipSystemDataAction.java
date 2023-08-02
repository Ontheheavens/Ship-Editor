package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.ShipSystemsLoaded;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;
import oth.shipeditor.menubar.FileUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 01.08.2023
 */
@Log4j2
public class LoadShipSystemDataAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        Path targetFile = Paths.get("data", "shipsystems", "ship_systems.csv");

        Map<Path, File> systemsDataPackage = FileUtilities.getFileFromPackages(targetFile);

        for (Path directory : systemsDataPackage.keySet()) {
            log.info("Ship systems table found in directory: {}", directory);
        }

        Map<String, List<ShipSystemCSVEntry>> systemsByPackage = new HashMap<>();
        for (Map.Entry<Path, File> folder : systemsDataPackage.entrySet()) {
            List<ShipSystemCSVEntry> systemsList = LoadShipSystemDataAction.loadShipSystemPackage(folder.getKey(),
                    folder.getValue());

            systemsByPackage.putIfAbsent(String.valueOf(folder.getKey()), systemsList);
        }
        EventBus.publish(new ShipSystemsLoaded(systemsByPackage));
    }

    private static List<ShipSystemCSVEntry> loadShipSystemPackage(Path folderPath, File table) {
        Path hullmodDataPath = table.toPath();

        log.info("Parsing shipsystem CSV data from: {}..", hullmodDataPath);
        List<Map<String, String>> csvData = FileLoading.parseCSVTable(hullmodDataPath);

        List<ShipSystemCSVEntry> shipSystemList = new ArrayList<>(csvData.size());
        for (Map<String, String> row : csvData) {
            String rowId = row.get("id");
            if (rowId != null && !rowId.isEmpty()) {
                ShipSystemCSVEntry newEntry = new ShipSystemCSVEntry(row, folderPath, hullmodDataPath);
                shipSystemList.add(newEntry);
            }
        }
        return shipSystemList;
    }


}
