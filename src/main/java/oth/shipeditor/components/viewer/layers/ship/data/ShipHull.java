package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.HullStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 31.07.2023
 */
@Log4j2
@Getter @Setter
public class ShipHull {

    private HullStyle hullStyle;

    private List<HullmodCSVEntry> builtInMods;

    private HullSpecFile associatedSpecFile;

    public void initialize(HullSpecFile specFile) {
        this.associatedSpecFile = specFile;
        this.reloadHullStyle();
        this.reloadBuiltInMods();
    }

    public void reloadHullStyle() {
        HullSpecFile specFile = getAssociatedSpecFile();
        String styleID = specFile.getStyle();
        GameDataRepository dataRepository = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = dataRepository.getAllHullStyles();
        HullStyle style = null;
        if (allHullStyles != null) {
            style = allHullStyles.get(styleID);
        }
        this.hullStyle = style;
    }

    public void reloadBuiltInMods() {
        HullSpecFile specFile = getAssociatedSpecFile();
        String[] specFileBuiltInMods = specFile.getBuiltInMods();
        if (specFileBuiltInMods == null) return;
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, HullmodCSVEntry> allHullmodEntries = gameData.getAllHullmodEntries();
        List<HullmodCSVEntry> builtInList = new ArrayList<>(specFileBuiltInMods.length);
        Stream<String> stream = Arrays.stream(specFileBuiltInMods);
        stream.forEach(hullmodID -> {
            HullmodCSVEntry hullmodEntry = allHullmodEntries.get(hullmodID);
            if (hullmodEntry != null) {
                builtInList.add(hullmodEntry);
            } else {
                log.error("Hullmod CSV entry not found for hullmod ID: {}", hullmodID);
            }
        });
        if (!builtInList.isEmpty()) {
            this.builtInMods = builtInList;
        }
    }

}
