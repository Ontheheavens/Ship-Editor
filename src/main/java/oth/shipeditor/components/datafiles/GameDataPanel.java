package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.components.datafiles.styles.EngineStylesPanel;
import oth.shipeditor.components.datafiles.styles.HullStylesPanel;
import oth.shipeditor.components.datafiles.trees.*;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    public GameDataPanel() {
        JTabbedPane container = new JTabbedPane(SwingConstants.BOTTOM);
        container.addTab("Hulls", new HullsTreePanel());
        container.addTab("Weapons", new WeaponsTreePanel());
        container.addTab(StringValues.HULLMODS, new HullmodsTreePanel());
        container.addTab("Shipsystems", new ShipSystemsTreePanel());
        container.addTab(StringValues.WINGS, new WingsTreePanel());
        container.addTab("Hull styles", new HullStylesPanel());
        container.addTab("Engine styles", new EngineStylesPanel());
        container.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setLayout(new BorderLayout());
        this.add(container, BorderLayout.CENTER);

        EventBus.subscribe(event -> {
            switch (event) {
                case SelectShipDataEntry ignored -> container.setSelectedIndex(0);
                case SelectWeaponDataEntry ignored -> container.setSelectedIndex(1);
                default -> {}
            }
        });
    }

}
