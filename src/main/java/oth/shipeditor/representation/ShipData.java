package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for holding of all raw deserialized ship data from JSON. Not intended to be altered by editing workflow.
 * @author Ontheheavens
 * @since 05.05.2023
 */
@Getter @Setter
public class ShipData {

    private HullSpecFile hullSpecFile;

    /**
     * Keys are skin IDs.
     */
    private final Map<String, SkinSpecFile> skins;

    public ShipData(HullSpecFile openedHullSpecFile) {
        this.hullSpecFile = openedHullSpecFile;
        this.skins = new HashMap<>();
        this.skins.put(SkinSpecFile.DEFAULT, SkinSpecFile.empty());
    }

    public String getHullFileName() {
        return String.valueOf(hullSpecFile.getFilePath().getFileName());
    }

}
