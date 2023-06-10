package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Ontheheavens
 * @since 06.06.2023
 */
final class HullPointsPanel extends JPanel {

    // TODO: Implement this whole panel. It should include ship center and shield center, and their radii.
    //  Should also probably include general settings of the layer, like sprite/points opacity.

    HullPointsPanel() {
        this.setBorder(new EmptyBorder(0, 6, 4, 6));
        LayoutManager layout = new GridLayout(3, 1);
        this.setLayout(layout);
        JPanel layerSettingsPanel = this.createLayerPanel();
        this.add(layerSettingsPanel);
        JPanel hullCenterPanel = new JPanel();
        hullCenterPanel.setBorder(BorderFactory.createTitledBorder("Collision"));
        this.add(hullCenterPanel);
        JPanel shieldCenterPanel = new JPanel();
        shieldCenterPanel.setBorder(BorderFactory.createTitledBorder("Shield"));
        this.add(shieldCenterPanel);
    }

    // TODO: Layer instruments should probably be their own tab.

    private JPanel createLayerPanel() {
        JPanel layerSettingsPanel = new JPanel();
        layerSettingsPanel.setBorder(BorderFactory.createTitledBorder("Layer"));
        JSlider opacitySlider = new JSlider(SwingConstants.HORIZONTAL,
                0, 100, 100);
        opacitySlider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            float changedValue = source.getValue() / 100.0f;
            EventBus.publish(new LayerOpacityChangeQueued(changedValue));
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                LayerPainter painter = selected.getPainter();
                if (painter == null) {
                    opacitySlider.setValue(100);
                } else {
                    opacitySlider.setValue((int) (painter.getSpriteOpacity() * 100.0f));
                }
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
        layerSettingsPanel.add(opacitySlider);
        return layerSettingsPanel;
    }

}
