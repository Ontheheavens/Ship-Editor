package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.viewer.InstrumentMode;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

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
    private BoundPointsPanel boundsPanel;

    private HullPointsPanel centerPointsPanel;

    private LayerPropertiesPanel layerPanel;

    private Map<JPanel, InstrumentMode> panelMode;

    public InstrumentTabsPane() {
        panelMode = new HashMap<>();
        this.initListeners();
        this.setTabPlacement(SwingConstants.LEFT);
        this.createTabs();
    }

    private void initListeners() {
        this.addChangeListener(event -> {
            activePanel = (JPanel) getSelectedComponent();
            this.dispatchModeChange(activePanel);
        });
    }

    private void createTabs() {
        layerPanel = new LayerPropertiesPanel();
        panelMode.put(layerPanel, InstrumentMode.LAYER);
        this.addTab("Layer",layerPanel);
        centerPointsPanel = new HullPointsPanel();
        panelMode.put(centerPointsPanel, InstrumentMode.CENTERS);
        this.addTab("Centers",centerPointsPanel);
        boundsPanel = new BoundPointsPanel();
        panelMode.put(boundsPanel, InstrumentMode.BOUNDS);
        this.addTab("Bounds", boundsPanel);
    }

    private void dispatchModeChange(JPanel active) {
        InstrumentMode selected = panelMode.get(active);
        EventBus.publish(new InstrumentModeChanged(selected));
        EventBus.publish(new ViewerRepaintQueued());
    }

}
