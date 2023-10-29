package oth.shipeditor.components.instrument.ship.skins;

import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.utility.components.containers.PropertiesPanel;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 28.10.2023
 */
public class SkinInfoPanel extends PropertiesPanel {

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public void refreshContent(ViewerLayer layer) {
        fireClearingListeners(layer);

        boolean layerPainterPresent = layer != null && layer.getPainter() != null;
        if (!layerPainterPresent) return;
        LayerPainter layerPainter = layer.getPainter();
        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) return;
        ShipLayer shipLayer = (ShipLayer) layer;
        ShipSkin activeSkin = shipLayer.getActiveSkin();
        if (activeSkin == null || activeSkin.isBase()) return;

        fireRefresherListeners(shipLayer);
    }

    protected void populateContent() {
        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var hullNameWidget = createHullNameEditor();
        widgets.put(hullNameWidget.getFirst(), hullNameWidget.getSecond());

        installWidgets(widgets);
    }

    private Pair<JLabel, JTextField> createHullNameEditor() {
        JTextField hullNameEditor = new JTextField();
        hullNameEditor.setToolTipText(StringValues.ENTER_TO_SAVE_CHANGES);
        hullNameEditor.setColumns(10);
        hullNameEditor.addActionListener(e -> {
            if (isWidgetsReadyForInput()) {
                String currentText = hullNameEditor.getText();
                ViewerLayer viewerLayer = getCachedLayer();
                if (viewerLayer instanceof ShipLayer shipLayer) {
                    var activeSkin = shipLayer.getActiveSkin();
                    if (activeSkin != null) {
                        activeSkin.setHullName(currentText);
                        processChange();
                    }
                }
            }
        });

        registerWidgetListeners(hullNameEditor, layer -> {
            hullNameEditor.setText(StringValues.NOT_INITIALIZED);
            hullNameEditor.setEnabled(false);
        }, layer -> {
            ShipLayer shipLayer = (ShipLayer) layer;
            var skin = shipLayer.getActiveSkin();
            hullNameEditor.setEnabled(true);
            hullNameEditor.setText(skin.getHullName());
        });

        return new Pair<>(new JLabel("Hull name:"), hullNameEditor);
    }

}
