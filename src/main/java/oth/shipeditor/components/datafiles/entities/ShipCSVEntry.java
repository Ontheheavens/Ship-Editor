package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import oth.shipeditor.representation.Hull;

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
    private final String hullID;

    @Getter
    private final Path packageFolder;

    public ShipCSVEntry(Map<String, String> row, Hull shipFile, Path folder) {
        this.packageFolder = folder;
        this.hullFile = shipFile;
        this.rowData = row;
        this.hullID = row.get("id");
    }

    @Override
    public String toString() {
        String displayedName = rowData.get("name");
        if (displayedName.isEmpty()) {
            displayedName = rowData.get("designation");
        }
        return displayedName + " (" + hullID + ")";
    }

}
