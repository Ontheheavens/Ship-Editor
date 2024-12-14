package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.DataTreesReloadQueued;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.communication.events.components.VariantDataTabSelected;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.datafiles.styles.EngineStylesPanel;
import oth.shipeditor.components.datafiles.styles.HullStylesPanel;
import oth.shipeditor.components.datafiles.trees.*;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.variant.VariantDataTab;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    private final JTabbedPane dataTabsContainer;
    private final HullsTreePanel hullsTreePanel;
    private final WeaponsTreePanel weaponsTreePanel;
    private final HullmodsTreePanel hullmodsTreePanel;
    private final ShipSystemsTreePanel systemsTreePanel;
    private final WingsTreePanel wingsTreePanel;

    public GameDataPanel() {
        dataTabsContainer = new JTabbedPane(SwingConstants.BOTTOM);
        hullsTreePanel = new HullsTreePanel();
        dataTabsContainer.addTab("Hulls", hullsTreePanel);
        weaponsTreePanel = new WeaponsTreePanel();
        dataTabsContainer.addTab("Weapons", weaponsTreePanel);
        hullmodsTreePanel = new HullmodsTreePanel();
        dataTabsContainer.addTab(StringValues.HULLMODS, hullmodsTreePanel);
        systemsTreePanel = new ShipSystemsTreePanel();
        dataTabsContainer.addTab("Shipsystems", systemsTreePanel);
        wingsTreePanel = new WingsTreePanel();
        dataTabsContainer.addTab(StringValues.WINGS, wingsTreePanel);
        dataTabsContainer.addTab("Hull styles", new HullStylesPanel());
        dataTabsContainer.addTab("Engine styles", new EngineStylesPanel());
        dataTabsContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setLayout(new BorderLayout());
        this.add(dataTabsContainer, BorderLayout.CENTER);

        this.initEventListening();
    }

    private void initEventListening() {
        EventBus.subscribe(event -> {
            // Looks ugly enough, Java 21's pattern matching was here,
            // but was cut out for Alex's compatibility.
            if (event instanceof SelectShipDataEntry) {
                selectShipTab();
            } else if (event instanceof SelectWeaponDataEntry) {
                selectWeaponTab();
            } else if (event instanceof InstrumentModeChanged updated) {
                handleInstrumentModeChange(updated.newMode());
            } else if (event instanceof VariantDataTabSelected tabEvent) {
                VariantDataTab variantDataTab = tabEvent.selected();
                if (variantDataTab == VariantDataTab.HULLMODS) {
                    selectHullmodsTab();
                } else if (variantDataTab == VariantDataTab.WINGS) {
                    selectWingsTab();
                }
            } else if (event instanceof DataTreesReloadQueued treesReload) {
                hullsTreePanel.reload();
                weaponsTreePanel.reload();
                hullmodsTreePanel.reload();
                systemsTreePanel.reload();
                wingsTreePanel.reload();
            }
        });
    }

    private void handleInstrumentModeChange(EditorInstrument newMode) {
        switch (newMode) {
            case BUILT_IN_MODS -> selectHullmodsTab();
            case BUILT_IN_WINGS -> selectWingsTab();
            case BUILT_IN_WEAPONS, DECORATIVES, VARIANT_WEAPONS -> selectWeaponTab();
            case VARIANT_MODULES -> selectShipTab();
            default -> {}
        }
    }

    private void selectShipTab() {
        dataTabsContainer.setSelectedIndex(0);
    }

    private void selectWeaponTab() {
        dataTabsContainer.setSelectedIndex(1);
    }

    private void selectHullmodsTab() {
        dataTabsContainer.setSelectedIndex(2);
    }

    private void selectWingsTab() {
        dataTabsContainer.setSelectedIndex(4);
    }

}
