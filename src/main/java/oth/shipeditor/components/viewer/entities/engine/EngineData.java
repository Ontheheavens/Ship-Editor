package oth.shipeditor.components.viewer.entities.engine;

import oth.shipeditor.representation.ship.EngineStyle;

/**
 * @author Ontheheavens
 * @since 21.11.2023
 */
public interface EngineData {

    Double getAngleBoxed();

    Double getLengthBoxed();

    Double getWidthBoxed();

    Double getContrailSizeBoxed();

    String getStyleID();

    EngineStyle getStyle();

}
