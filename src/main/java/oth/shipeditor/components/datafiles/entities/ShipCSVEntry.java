package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Getter
public class ShipCSVEntry {

    private final Map<String, String> rowData;

    private final Hull hullFile;

    private final List<Skin> skins;

    @Setter
    private Skin activeSkin;

    private final String hullFileName;

    private final String hullID;

    private final Path packageFolder;

    public ShipCSVEntry(Map<String, String> row, Map.Entry<Hull, List<Skin>> hullWithSkins,
                        Path folder, String fileName) {
        this.packageFolder = folder;
        this.hullFile = hullWithSkins.getKey();
        this.skins = hullWithSkins.getValue();
        this.rowData = row;
        this.hullID = row.get(StringConstants.ID);
        this.hullFileName = fileName;
        this.activeSkin = Skin.empty();
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
