package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;

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
        container.addTab("Hullmods", new HullmodsTreePanel());
        container.addTab("Hull styles", new HullStylesPanel());
        container.addTab("Shipsystems", new ShipSystemsTreePanel());
        container.addTab("Wings", new WingsTreePanel());
        container.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setLayout(new BorderLayout());
        this.add(container, BorderLayout.CENTER);
    }

}
