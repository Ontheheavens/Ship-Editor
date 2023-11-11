package oth.shipeditor.components.instrument.ship.skins;

import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 28.10.2023
 */
public class SkinInfoPanel extends LayerPropertiesPanel {

    public void refreshContent(LayerPainter layerPainter) {
        fireClearingListeners(layerPainter);

        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) return;
        ShipLayer shipLayer = shipPainter.getParentLayer();
        ShipSkin activeSkin = shipLayer.getActiveSkin();
        if (activeSkin == null || activeSkin.isBase()) return;

        fireRefresherListeners(layerPainter);
    }

    protected void populateContent() {
        this.setLayout(new BorderLayout());
        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var hullNameWidget = createHullNameEditor();
        widgets.put(hullNameWidget.getFirst(), hullNameWidget.getSecond());

        JPanel widgetsPanel = createWidgetsPanel(widgets);
        this.add(widgetsPanel, BorderLayout.PAGE_START);
    }

    @Override
    protected JPanel createWidgetsPanel(Map<JLabel, JComponent> widgets) {
        JPanel widgetsPanel = super.createWidgetsPanel(widgets);
        ComponentUtilities.outfitPanelWithTitle(widgetsPanel, "Skin data");
        return widgetsPanel;
    }

    private Pair<JLabel, JTextField> createHullNameEditor() {
        JTextField hullNameEditor = new JTextField();
        hullNameEditor.setToolTipText(StringValues.ENTER_TO_SAVE_CHANGES);
        hullNameEditor.setColumns(10);
        hullNameEditor.addActionListener(e -> {
            if (isWidgetsReadyForInput()) {
                String currentText = hullNameEditor.getText();
                LayerPainter cachedLayer = getCachedLayerPainter();
                ViewerLayer viewerLayer = cachedLayer.getParentLayer();
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
        }, layerPainter -> {
            ShipLayer shipLayer = (ShipLayer) layerPainter.getParentLayer();
            var skin = shipLayer.getActiveSkin();
            hullNameEditor.setEnabled(true);
            hullNameEditor.setText(skin.getHullName());
        });

        return new Pair<>(new JLabel("Hull name:"), hullNameEditor);
    }

}
