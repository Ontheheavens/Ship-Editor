package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.HullStyle;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 31.07.2023
 */
@Getter @Setter
public class ShipHull {

    // TODO: This will be a dedicated runtime hull data instance.

    private HullStyle hullStyle;

    public void initHullStyle(HullSpecFile specFile) {
        String styleID = specFile.getStyle();
        GameDataRepository dataRepository = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = dataRepository.getAllHullStyles();
        HullStyle style = null;
        if (allHullStyles != null) {
            style = allHullStyles.get(styleID);
        }
        this.hullStyle = style;
    }

}
