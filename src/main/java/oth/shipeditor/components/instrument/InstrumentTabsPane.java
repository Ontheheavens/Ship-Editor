package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 08.06.2023
 */
@Log4j2
public final class InstrumentTabsPane extends JTabbedPane {


    /**
     * Panel that is currently selected; depending on which panel it is interactivity of certain entities is resolved.
     */
    private JPanel activePanel;

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private final BoundPointsPanel boundsPanel;

    private final HullPointsPanel centerPointsPanel;

    private final LayerPropertiesPanel layerPanel;

    public InstrumentTabsPane() {
        this.addChangeListener(event -> {
            activePanel = (JPanel) getSelectedComponent();
        });
        this.setTabPlacement(SwingConstants.LEFT);
        layerPanel = new LayerPropertiesPanel();
        this.addTab("Layer",layerPanel);
        centerPointsPanel = new HullPointsPanel();
        this.addTab("Centers",centerPointsPanel);
        boundsPanel = new BoundPointsPanel();
        this.addTab("Bounds", boundsPanel);
    }

}
