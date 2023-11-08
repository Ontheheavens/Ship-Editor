package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerOpacityChangeQueued;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.LayerPropertiesPanel;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 07.11.2023
 */
public class LayerCircumstancePanel extends LayerPropertiesPanel {

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        fireClearingListeners(layerPainter);

        boolean uninitialized = layerPainter instanceof ShipPainter shipPainter && shipPainter.isUninitialized();
        if (layerPainter == null || uninitialized) return;

        fireRefresherListeners(layerPainter);
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());
        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var layerOpacityWidget = createLayerOpacitySlider();
        widgets.put(layerOpacityWidget.getFirst(), layerOpacityWidget.getSecond());

        JPanel widgetsPanel = createWidgetsPanel(widgets);
        this.add(widgetsPanel, BorderLayout.PAGE_START);
    }

    private Pair<JLabel, JSlider> createLayerOpacitySlider() {
        Pair<JLabel, JSlider> baseWidgets = ComponentUtilities.createOpacityWidget();

        JLabel opacityLabel = baseWidgets.getFirst();
        opacityLabel.setText("Sprite opacity:");
        JSlider opacitySlider = baseWidgets.getSecond();

        opacitySlider.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                int opacity = opacitySlider.getValue();
                opacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
                float changedValue = opacity / 100.0f;
                EventBus.publish(new LayerOpacityChangeQueued(changedValue));
                processChange();
            }
        });

        registerWidgetListeners(opacitySlider, layer -> {
            opacitySlider.setValue(100);
            opacitySlider.setEnabled(false);
            opacityLabel.setToolTipText(StringValues.NOT_INITIALIZED);
        }, layerPainter -> {
            // Refresh code is expected to make sure this block never gets called if layer does not have a painter.
            int value = (int) (layerPainter.getSpriteOpacity() * 100.0f);
            opacityLabel.setToolTipText(StringValues.CURRENT_VALUE + value + "%");
            opacitySlider.setValue(value);
            opacitySlider.setEnabled(true);
        });

        return baseWidgets;
    }

}
