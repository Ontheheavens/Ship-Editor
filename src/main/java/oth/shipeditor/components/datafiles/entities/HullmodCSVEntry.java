package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.HullSize;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@Log4j2
@Getter
public class HullmodCSVEntry implements OrdnancedCSVEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String hullmodID;

    private final String spriteFileName;

    private File fetchedSpriteFile;

    public HullmodCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        hullmodID = this.rowData.get("id");
        spriteFileName = this.rowData.get(StringConstants.SPRITE);
    }

    @Override
    public String getID() {
        return hullmodID;
    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = StringValues.UNTITLED;
        }
        return displayedName;
    }

    @Override
    public int getOrdnanceCost(HullSize size) {
        var csvData = this.getRowData();
        String stringValue;
        switch (size) {
            case FRIGATE -> stringValue = csvData.get("cost_frigate");
            case DESTROYER -> stringValue = csvData.get("cost_dest");
            case CRUISER -> stringValue = csvData.get("cost_cruiser");
            case CAPITAL_SHIP -> stringValue = csvData.get("cost_capital");
            default -> stringValue = "0";
        }
        return Integer.parseInt(stringValue);
    }

    @Override
    public JLabel getIconLabel() {
        return getIconLabel(32);
    }

    @Override
    public JLabel getIconLabel(int maxSize) {
        Map<String, String> csvData = this.getRowData();
        String name = csvData.get("name");
        File imageFile = this.fetchHullmodSpriteFile();
        BufferedImage iconImage = FileLoading.loadSpriteAsImage(imageFile);
        return ComponentUtilities.createIconFromImage(iconImage, name, maxSize);
    }

    private File fetchHullmodSpriteFile() {
        if (fetchedSpriteFile != null) {
            return fetchedSpriteFile;
        }
        String path = this.spriteFileName;
        if (path == null || path.isEmpty()) {
            // Fallback sprite with question mark.
            path = "graphics/icons/intel/investigation.png";
        }
        Path spritePath = Path.of(path);
        fetchedSpriteFile = FileLoading.fetchDataFile(spritePath, packageFolderPath);
        return fetchedSpriteFile;
    }

}
