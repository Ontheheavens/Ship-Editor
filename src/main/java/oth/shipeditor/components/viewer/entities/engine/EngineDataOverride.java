package oth.shipeditor.components.viewer.entities.engine;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.representation.GameDataRepository;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 22.08.2023
 */
@Getter @Setter @Builder
public class EngineDataOverride {

    private Integer index;

    private Double angle;

    private Double length;

    private Double width;

    private String styleID;

    private EngineStyle style;

    public EngineStyle getStyle() {
        if (style != null) return style;
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, EngineStyle> allEngineStyles = gameData.getAllEngineStyles();
        if (allEngineStyles != null) {
            EngineStyle engineStyle = allEngineStyles.get(styleID);
            this.setStyle(engineStyle);
        }
        return style;
    }

}
