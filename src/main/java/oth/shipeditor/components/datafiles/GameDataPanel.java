package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.Files;
import oth.shipeditor.representation.Hull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    public GameDataPanel() {
        this.setLayout(new BorderLayout());
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel("Game data"));
        JButton loadCSVButton = new JButton(new LoadShipDataAction());
        loadCSVButton.setActionCommand(Files.STARSECTOR_CORE);
        loadCSVButton.setText("Load ship data");
        topContainer.add(loadCSVButton);
        this.add(topContainer, BorderLayout.PAGE_START);
        HullsTree hullsTree = new HullsTree();
        JSplitPane treeSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeSplitter.setOneTouchExpandable(true);
        treeSplitter.setResizeWeight(0.4f);
        treeSplitter.setLeftComponent(hullsTree);
        JPanel dumdumPane = new JPanel();
        dumdumPane.add(new JLabel("Entry Content"));
        treeSplitter.setRightComponent(dumdumPane);
        this.add(treeSplitter, BorderLayout.CENTER);
    }

}
