package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.Files;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    private JPanel entryDataPanel;

    public GameDataPanel() {
        this.setLayout(new BorderLayout());
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel("Game data"));
        JButton loadCSVButton = new JButton(new LoadShipDataAction());
        loadCSVButton.setActionCommand(Files.STARSECTOR_CORE);
        loadCSVButton.setText("Load ship data");
        topContainer.add(loadCSVButton);
        this.add(topContainer, BorderLayout.PAGE_START);
        JSplitPane splitPane = createContentSplitter();
        this.add(splitPane, BorderLayout.CENTER);
    }

    private JSplitPane createContentSplitter() {
        HullsTree hullsTree = new HullsTree();
        JTree tree = hullsTree.getHullsTree();
        entryDataPanel = new JPanel();
        entryDataPanel.add(new JLabel("Entry Content"));
        entryDataPanel.setLayout(new BoxLayout(entryDataPanel, BoxLayout.PAGE_AXIS));
        entryDataPanel.setAlignmentX(CENTER_ALIGNMENT);
        entryDataPanel.setBorder(BorderFactory.createEmptyBorder(2,6, 2, 2));
        tree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            if (node.getUserObject() instanceof ShipCSVEntry checked) {
                log.info(checked);
                entryDataPanel.removeAll();
                Map<String, String> data = checked.getRowData();
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    JLabel keyLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
                    entryDataPanel.add(keyLabel);
                }
                entryDataPanel.revalidate();
                entryDataPanel.repaint();
            }
        });
        JScrollPane dataScrollContainer = new JScrollPane(entryDataPanel);
        JSplitPane treeSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeSplitter.setOneTouchExpandable(true);
        treeSplitter.setResizeWeight(0.4f);
        treeSplitter.setLeftComponent(hullsTree);
        treeSplitter.setRightComponent(dataScrollContainer);
        return treeSplitter;
    }

}
