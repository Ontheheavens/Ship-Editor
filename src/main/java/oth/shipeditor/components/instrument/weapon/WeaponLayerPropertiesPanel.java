package oth.shipeditor.components.instrument.weapon;

import oth.shipeditor.components.instrument.AbstractLayerPropertiesPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;

/**
 * @author Ontheheavens
 * @since 09.11.2023
 */
public class WeaponLayerPropertiesPanel extends AbstractLayerPropertiesPanel {

    @Override
    protected boolean isValidLayer(LayerPainter layerPainter) {
        return layerPainter instanceof WeaponPainter;
    }

    @Override
    protected void clearData() {

    }

    @Override
    protected void refreshData(ViewerLayer selected) {

    }

}
