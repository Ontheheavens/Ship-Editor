package oth.shipeditor.components.datafiles.entities;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
public class ShipCSVEntry {

    private final Map<String, String> rowData;

    private final String hullID;

    public ShipCSVEntry(Map<String, String> row) {
        this.rowData = row;
        this.hullID = row.get("id");
    }

    @Override
    public String toString() {
        return rowData.get("name") + " (" + hullID + ")";
    }

}
