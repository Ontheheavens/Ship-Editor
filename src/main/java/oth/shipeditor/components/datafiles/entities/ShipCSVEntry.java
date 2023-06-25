package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
public class ShipCSVEntry {

    @Getter
    private final Map<String, String> rowData;

    private final String hullID;

    public ShipCSVEntry(Map<String, String> row) {
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
