package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@Getter
public class WeaponCSVEntry implements CSVEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String weaponID;

    @Setter
    private WeaponSpecFile specFile;

    public WeaponCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        weaponID = this.rowData.get("id");
    }

    @Override
    public String getID() {
        return weaponID;
    }


    public void loadLayerFromEntry() {

    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = StringValues.UNTITLED;
        }
        return displayedName;
    }

}
