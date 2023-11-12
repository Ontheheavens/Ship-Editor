package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
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

    public GameDataPanel() {
        dataTabsContainer = new JTabbedPane(SwingConstants.BOTTOM);
        dataTabsContainer.addTab("Hulls", new HullsTreePanel());
        dataTabsContainer.addTab("Weapons", new WeaponsTreePanel());
        dataTabsContainer.addTab(StringValues.HULLMODS, new HullmodsTreePanel());
        dataTabsContainer.addTab("Shipsystems", new ShipSystemsTreePanel());
        dataTabsContainer.addTab(StringValues.WINGS, new WingsTreePanel());
        dataTabsContainer.addTab("Hull styles", new HullStylesPanel());
        dataTabsContainer.addTab("Engine styles", new EngineStylesPanel());
        dataTabsContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setLayout(new BorderLayout());
        this.add(dataTabsContainer, BorderLayout.CENTER);

        this.initEventListening();
    }

    private void initEventListening() {
        EventBus.subscribe(event -> {
            switch (event) {
                case SelectShipDataEntry ignored -> selectShipTab();
                case SelectWeaponDataEntry ignored -> selectWeaponTab();
                case InstrumentModeChanged updated -> handleInstrumentModeChange(updated.newMode());
                case VariantDataTabSelected tabEvent -> {
                    VariantDataTab variantDataTab = tabEvent.selected();
                    if (variantDataTab == VariantDataTab.HULLMODS) {
                        selectHullmodsTab();
                    } else if (variantDataTab == VariantDataTab.WINGS) {
                        selectWingsTab();
                    }
                }
                default -> {}
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
