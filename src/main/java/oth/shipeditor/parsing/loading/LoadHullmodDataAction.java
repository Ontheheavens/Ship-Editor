package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodFoldersWalked;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 06.07.2023
 */
@Log4j2
public class LoadHullmodDataAction extends LoadCSVDataAction<HullmodCSVEntry> {

    LoadHullmodDataAction() {
        super(Paths.get("data", "hullmods", "hull_mods.csv"));
    }

    @Override
    protected void publishResult(Map<Path, List<HullmodCSVEntry>> entriesByPackage) {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setHullmodEntriesByPackage(entriesByPackage);
        EventBus.publish(new HullmodFoldersWalked(entriesByPackage));
    }

    @Override
    protected HullmodCSVEntry instantiateEntry(Map<String, String> row, Path folderPath, Path dataFilePath) {
        return new HullmodCSVEntry(row, folderPath, dataFilePath);
    }

}
