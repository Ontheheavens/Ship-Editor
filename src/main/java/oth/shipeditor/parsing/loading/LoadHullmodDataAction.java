package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodFoldersWalked;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
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
 * @since 06.07.2023
 */
@Log4j2
public class LoadHullmodDataAction extends AbstractAction {

    private static final String HULLMODS = "hullmods";
    private static final String HULL_MODS_CSV = "hull_mods.csv";

    @Override
    public void actionPerformed(ActionEvent e) {
        Path targetFile = Paths.get("data", HULLMODS, HULL_MODS_CSV);

        Map<Path, File> hullmodDataPackage = FileUtilities.getFileFromPackages(targetFile);

        if (hullmodDataPackage.isEmpty()) {
            log.info("Hullmod data file not found in any directory.");
        } else {
            for (Path directory : hullmodDataPackage.keySet()) {
                log.info("Hullmod data file found in directory: {}", directory);
            }
        }

        Map<String, List<HullmodCSVEntry>> hullmodsByPackage = new HashMap<>();
        for (Path folder : hullmodDataPackage.keySet()) {
            List<HullmodCSVEntry> hullmodsList = LoadHullmodDataAction.loadHullmodPackage(folder);
            hullmodsByPackage.putIfAbsent(String.valueOf(folder), hullmodsList);
        }
        EventBus.publish(new HullmodFoldersWalked(hullmodsByPackage));
    }

    private static List<HullmodCSVEntry> loadHullmodPackage(Path folderPath) {
        Path hullmodDataPath = Paths.get(String.valueOf(folderPath), "data", HULLMODS, HULL_MODS_CSV);

        log.info("Parsing hullmod CSV data from: {}..", hullmodDataPath);
        List<Map<String, String>> csvData = FileLoading.parseCSVTable(hullmodDataPath);
        log.info("Hullmod CSV data from {} retrieved successfully.", hullmodDataPath);

        List<HullmodCSVEntry> hullmodsList = new ArrayList<>(csvData.size());
        for (Map<String, String> row : csvData) {
            String rowId = row.get("id");
            if (rowId != null && !rowId.isEmpty()) {
                HullmodCSVEntry newEntry = new HullmodCSVEntry(row, folderPath, hullmodDataPath);
                hullmodsList.add(newEntry);
            }
        }
        return hullmodsList;
    }

}
