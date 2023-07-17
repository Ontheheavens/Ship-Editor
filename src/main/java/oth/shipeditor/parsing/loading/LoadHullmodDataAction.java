package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodFoldersWalked;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 06.07.2023
 */
@Log4j2
public class LoadHullmodDataAction extends AbstractAction {

    private static final String HULLMODS = "hullmods";
    private static final String HULL_MODS_CSV = "hull_mods.csv";

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void actionPerformed(ActionEvent e) {
        Settings settings = SettingsManager.getSettings();
        Collection<String> eligibleFolders = new ArrayList<>();
        eligibleFolders.add(settings.getCoreFolderPath());

        String modFolderPath = settings.getModFolderPath();
        Path targetFile = Paths.get("data", HULLMODS, HULL_MODS_CSV);

        Collection<Path> modsWithHullmodData = new ArrayList<>();

        try (Stream<Path> childDirectories = Files.list(Paths.get(modFolderPath))) {
            childDirectories
                    .filter(Files::isDirectory)
                    .forEach(childDir -> {
                        Path targetFilePath = childDir.resolve(targetFile);
                        if (Files.exists(targetFilePath)) {
                            modsWithHullmodData.add(childDir);
                        }
                    });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if (modsWithHullmodData.isEmpty()) {
            log.info("Hullmod data file not found in any mod directory.");
        } else {
            for (Path directory : modsWithHullmodData) {
                log.info("Hullmod data file found in mod directory: {}", directory);
                eligibleFolders.add(directory.toString());
            }
        }

        Map<String, List<HullmodCSVEntry>> hullmodsByPackage = new HashMap<>();
        for (String folder : eligibleFolders) {
            List<HullmodCSVEntry> hullmodsList = LoadHullmodDataAction.loadHullmodPackage(folder);
            hullmodsByPackage.putIfAbsent(folder, hullmodsList);
        }
        EventBus.publish(new HullmodFoldersWalked(hullmodsByPackage));
    }

    private static List<HullmodCSVEntry> loadHullmodPackage(String folderPath) {
        Path hullmodDataPath = Paths.get(folderPath, "data", HULLMODS, HULL_MODS_CSV);

        log.info("Parsing hullmod CSV data from: {}..", hullmodDataPath);
        List<Map<String, String>> csvData = FileLoading.parseCSVTable(hullmodDataPath);
        log.info("Hullmod CSV data from {} retrieved successfully.", hullmodDataPath);

        List<HullmodCSVEntry> hullmodsList = new ArrayList<>(csvData.size());
        for (Map<String, String> row : csvData) {
            String rowId = row.get("id");
            if (rowId != null && !rowId.isEmpty()) {
                HullmodCSVEntry newEntry = new HullmodCSVEntry(row, Path.of(folderPath), hullmodDataPath);
                hullmodsList.add(newEntry);
            }
        }
        return hullmodsList;
    }

}
