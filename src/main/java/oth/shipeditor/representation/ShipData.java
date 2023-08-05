package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.persistence.SettingsManager;

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

    // TODO: this needs to be moved into dedicated runtime hull data class.
    private HullStyle hullStyle;

    public ShipData(HullSpecFile openedHullSpecFile) {
        this.hullSpecFile = openedHullSpecFile;
        this.skins = new HashMap<>();
        this.skins.put(SkinSpecFile.DEFAULT, SkinSpecFile.empty());
    }

    public void initHullStyle() {
        String styleID = hullSpecFile.getStyle();
        GameDataRepository dataRepository = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = dataRepository.getAllHullStyles();
        HullStyle style = null;
        if (allHullStyles != null) {
            style = allHullStyles.get(styleID);
        }
        this.hullStyle = style;
    }

    // TODO: Refactor raw JSON-class Skin into runtime-adapted SkinData, so we can have easy interaction with the instance.

    public void addSkin(SkinSpecFile skinSpecFile) {
        this.skins.put(skinSpecFile.getSkinHullId(), skinSpecFile);
    }

}
