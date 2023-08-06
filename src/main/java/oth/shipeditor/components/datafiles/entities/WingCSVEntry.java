package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ShipSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.Variant;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 03.08.2023
 */
@Log4j2
@Getter
public class WingCSVEntry implements CSVEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String wingID;

    @Setter
    private String displayedName;

    public WingCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        wingID = this.rowData.get("id");
    }

    @Override
    public String getID() {
        return wingID;
    }

    @Override
    public String toString() {
        // TODO: get displayed name from variant.
        String name = rowData.get(StringConstants.ID);
        if (name.isEmpty()) {
            name = StringValues.UNTITLED;
        }
        return name;
    }

    public String getEntryName() {
        if (this.displayedName != null) {
            return this.displayedName;
        }
        String variantID = rowData.get(StringConstants.VARIANT);
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, Variant> allVariants = gameData.getAllVariants();
        Variant variant = allVariants.get(variantID);

        String hullID = variant.getHullId();

        ShipSpecFile desiredSpec = null;

        Map<String, ShipCSVEntry> allShipEntries = gameData.getAllShipEntries();
        outer: for (ShipCSVEntry shipEntry : allShipEntries.values()) {
            String shipEntryHullID = shipEntry.getHullID();
            if (shipEntryHullID.equals(hullID)) {
                desiredSpec = shipEntry.getHullSpecFile();
                break;
            } else {
                Map<String, SkinSpecFile> skins = shipEntry.getSkins();
                if (skins == null || skins.isEmpty()) continue;
                for (SkinSpecFile skinSpec : skins.values()) {
                    String skinHullId = skinSpec.getSkinHullId();
                    if (skinHullId != null && skinHullId.equals(hullID)) {
                        desiredSpec = skinSpec;
                        break outer;
                    }
                }
            }
        }

        if (desiredSpec != null) {
            String result = desiredSpec.getHullName() + " Wing";
            this.setDisplayedName(result);
            return result;
        }

        return this.getWingID();
    }

}
