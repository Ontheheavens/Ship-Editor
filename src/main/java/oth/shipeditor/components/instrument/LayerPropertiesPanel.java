package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;

import javax.swing.*;
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
        JPanel layerSettingsPanel = this.createLayerPanel();
        this.add(layerSettingsPanel);
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

    // TODO: Layer instruments should probably be their own tab.

    private JPanel createLayerPanel() {
        JPanel layerSettingsPanel = new JPanel();
        layerSettingsPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
        opacitySlider = new JSlider(SwingConstants.HORIZONTAL,
                0, 100, 100);
        opacitySlider.setEnabled(false);
        // TODO: Make slider panel more beautiful.
        opacitySlider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            int opacity = source.getValue();
            this.updateOpacityLabel(opacity);
            float changedValue = opacity / 100.0f;
            EventBus.publish(new LayerOpacityChangeQueued(changedValue));
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                if (selected == null) {
                    this.updateOpacityLabel(100);
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
                this.updateOpacityLabel(value);
                opacitySlider.setValue(value);
            }
        });
        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0%"));
        labelTable.put(50, new JLabel("50%"));
        labelTable.put(100, new JLabel("100%"));
        opacitySlider.setLabelTable(labelTable);
        opacitySlider.setMajorTickSpacing(50);
        opacitySlider.setMinorTickSpacing(10);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacityLabel = new JLabel();
        this.updateOpacityLabel(100);
        layerSettingsPanel.add(opacityLabel);
        layerSettingsPanel.add(opacitySlider);
        return layerSettingsPanel;
    }

}
