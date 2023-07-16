package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.viewer.layers.LayerOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Ontheheavens
 * @since 11.06.2023
 */
final class LayerPropertiesPanel extends JPanel {

    //  Should also probably include general settings of the layer, like sprite/points opacity.

    private JLabel opacityLabel;
    private JSlider opacitySlider;

    LayerPropertiesPanel() {
        this.setLayout(new BorderLayout());
        JPanel layerSettingsPanel = this.createLayerPanel();
        this.add(layerSettingsPanel, BorderLayout.CENTER);
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                opacitySlider.setEnabled(checked.selected() != null);
            }
        });
    }

    private void updateOpacityLabel(int opacity) {
        opacityLabel.setText("Sprite opacity: " + opacity + "%");
    }

    private JPanel createLayerPanel() {
        JPanel layerSettingsPanel = new JPanel();
        layerSettingsPanel.setLayout(new BoxLayout(layerSettingsPanel, BoxLayout.PAGE_AXIS));

        Border titled = BorderFactory.createTitledBorder(StringConstants.LAYER_PROPERTIES);
        Border outsideBorder = new EmptyBorder(0, 6, 6, 6);
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(outsideBorder, titled);
        layerSettingsPanel.setBorder(compoundBorder);

        this.addOpacityWidget(layerSettingsPanel);

        return layerSettingsPanel;
    }

    private void addOpacityWidget(JPanel layerSettingsPanel) {
        ChangeListener changeListener = e -> {
            JSlider source = (JSlider)e.getSource();
            int opacity = source.getValue();
            updateOpacityLabel(opacity);
            float changedValue = opacity / 100.0f;
            EventBus.publish(new LayerOpacityChangeQueued(changedValue));
        };
        BusEventListener eventListener = event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                if (selected == null) {
                    updateOpacityLabel(100);
                    opacitySlider.setValue(100);
                    return;
                }
                LayerPainter painter = selected.getPainter();
                int value;
                if (painter == null) {
                    value = 100;
                } else {
                    value = (int) (painter.getSpriteOpacity() * 100.0f);
                }
                updateOpacityLabel(value);
                opacitySlider.setValue(value);
            }
        };
        Pair<JSlider, JLabel> widgetComponents = Utility.createOpacityWidget(changeListener, eventListener);

        opacitySlider = widgetComponents.getFirst();
        opacityLabel = widgetComponents.getSecond();
        opacityLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        this.updateOpacityLabel(100);

        layerSettingsPanel.add(opacityLabel);
        layerSettingsPanel.add(opacitySlider);
    }

}
