package oth.shipeditor.components.instrument.weapon;

import oth.shipeditor.components.instrument.AbstractInstrumentsPane;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
public class WeaponInstrumentsPane extends AbstractInstrumentsPane {

    public WeaponInstrumentsPane() {
        createTabs();
        this.dispatchModeChange((JPanel) getSelectedComponent());
    }

    private void createTabs() {
        JPanel layerPanel = new JPanel();
        layerPanel.setLayout(new BorderLayout());

        JPanel layerWidgetsPanel = new WeaponLayerInfoPanel();
        layerPanel.add(layerWidgetsPanel, BorderLayout.CENTER);
        this.addTab(StringValues.LAYER,layerPanel);

        // TODO: not implemented.
        JPanel offsetPanel = new JPanel();
        String offsets = "Offsets";
        this.addTab(offsets,offsetPanel);

        // Temporary until weapon editing is implemented.
        int offsetsIndex = this.indexOfTab(offsets);
        this.setEnabledAt(offsetsIndex, false);
    }

    @Override
    protected void dispatchModeChange(JPanel active) {
        // TODO: not implemented.
    }

    @Override
    protected void updateTooltipText() {
        // TODO: not implemented.
    }

}
