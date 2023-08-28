package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.HullStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private List<WingCSVEntry> builtInWings;

    private HullSpecFile associatedSpecFile;

    public void initialize(HullSpecFile specFile) {
        this.associatedSpecFile = specFile;
        this.loadHullStyle();

        var dataRepository = SettingsManager.getGameData();
        if (dataRepository.isHullmodDataLoaded()) {
            this.loadBuiltInMods();
        }
        if (dataRepository.isWingDataLoaded()) {
            this.loadBuiltInWings();
        }
    }

    private void loadHullStyle() {
        var specFile = getAssociatedSpecFile();
        var styleID = specFile.getStyle();
        this.hullStyle = GameDataRepository.fetchStyleByID(styleID);
    }

    public void loadBuiltInMods() {
        if (builtInMods != null) return;
        var specFile = getAssociatedSpecFile();
        String[] specFileBuiltInMods = specFile.getBuiltInMods();
        if (specFileBuiltInMods == null) {
            this.builtInMods = new ArrayList<>();
            return;
        }
        var gameData = SettingsManager.getGameData();
        var allHullmodEntries = gameData.getAllHullmodEntries();
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
        this.builtInMods = builtInList;
    }

    public void loadBuiltInWings() {
        if (builtInWings != null) return;
        var specFile = getAssociatedSpecFile();
        String[] specFileBuiltInWings = specFile.getBuiltInWings();
        if (specFileBuiltInWings == null) {
            this.builtInWings = new ArrayList<>();
            return;
        }
        var gameData = SettingsManager.getGameData();
        var allWingEntries = gameData.getAllWingEntries();
        List<WingCSVEntry> builtInList = new ArrayList<>(specFileBuiltInWings.length);
        Stream<String> stream = Arrays.stream(specFileBuiltInWings);
        stream.forEach(wingID -> {
            WingCSVEntry wingCSVEntry = allWingEntries.get(wingID);
            if (wingCSVEntry != null) {
                builtInList.add(wingCSVEntry);
            } else {
                log.error("Wing CSV entry not found for wing ID: {}", wingID);
            }
        });
        this.builtInWings = builtInList;
    }

}
