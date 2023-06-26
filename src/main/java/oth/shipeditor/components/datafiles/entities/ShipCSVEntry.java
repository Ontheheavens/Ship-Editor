package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.utility.StringConstants;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
public class ShipCSVEntry {

    @Getter
    private final Map<String, String> rowData;

    @Getter
    private final Hull hullFile;

    @Getter
    private final String hullFileName;

    @Getter
    private final String hullID;

    @Getter
    private final Path packageFolder;

    public ShipCSVEntry(Map<String, String> row, Hull shipFile, Path folder, String fileName) {
        this.packageFolder = folder;
        this.hullFile = shipFile;
        this.rowData = row;
        this.hullID = row.get(StringConstants.ID);
        this.hullFileName = fileName;
    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = rowData.get(StringConstants.DESIGNATION);
        }
        return displayedName + " (" + hullID + ")";
    }

}
