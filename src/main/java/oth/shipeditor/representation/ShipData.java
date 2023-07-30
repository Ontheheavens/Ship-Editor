package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.persistence.SettingsManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
@Getter @Setter
public class ShipData {

    private Hull hull;

    /**
     * Keys are skin IDs.
     */
    private final Map<String, Skin> skins;
    private HullStyle hullStyle;

    public ShipData(Hull openedHull) {
        this.hull = openedHull;
        this.skins = new HashMap<>();
    }

    public void initHullStyle() {
        String styleID = hull.getStyle();
        GameDataRepository dataRepository = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = dataRepository.getAllHullStyles();
        HullStyle style = null;
        if (allHullStyles != null) {
            style = allHullStyles.get(styleID);
        }
        this.hullStyle = style;
    }

}
