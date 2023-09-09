package oth.shipeditor.components.viewer.painters.features;

import lombok.Getter;
import oth.shipeditor.components.viewer.layers.LayerPainter;

/**
 * @author Ontheheavens
 * @since 09.09.2023
 */
@Getter
public class InstalledFeature {

    private final String slotID;

    private final String featureID;

    private final LayerPainter featurePainter;

    public InstalledFeature(String slot, String id, LayerPainter painter) {
        this.slotID = slot;
        this.featureID = id;
        this.featurePainter = painter;
    }

}
