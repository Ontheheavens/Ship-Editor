package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodFoldersWalked;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@Log4j2
class HullmodsTreePanel extends DataTreePanel{

    HullmodsTreePanel() {
        super("Hullmod files");
    }

    @Override
    JPanel createTopPanel() {
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel("Hullmod data:"));
        JButton loadCSVButton = new JButton(FileUtilities.getLoadHullmodDataAction());
        loadCSVButton.setText("Reload hullmod data");
        loadCSVButton.setToolTipText("Reload all hullmod entries, grouped by package");
        topContainer.add(loadCSVButton);
        return topContainer;
    }

    @Override
    void initTreePanelListeners(JPanel passedTreePanel) {
        EventBus.subscribe(event -> {
            if (event instanceof HullmodFoldersWalked checked) {
                Map<String, List<HullmodCSVEntry>> hullmods = checked.hullmodsByPackage();
                if (hullmods == null) {
                    throw new RuntimeException("Hullmod data initialization failed: table data is NULL!");
                };
                loadAllHullmods(hullmods);
                sortAndExpandTree();
                repaint();
            }
        });
        initComponentListeners();
    }

    private void initComponentListeners() {
        JTree tree = getTree();
        tree.addMouseListener(new ContextMenuListener());
        tree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            if (selectedNode == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            if (node.getUserObject() instanceof HullmodCSVEntry checked) {
                updateEntryPanel(checked);
            }
        });
    }

    private void updateEntryPanel(HullmodCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 0, 5);
        String spriteFileName = selected.getSpriteFileName();
        if (spriteFileName != null && !spriteFileName.isEmpty()) {
            JPanel hullmodIconPanel = HullmodsTreePanel.createHullmodIconPanel(selected);
            rightPanel.add(hullmodIconPanel, constraints);
        }
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    private static JPanel createHullmodIconPanel(HullmodCSVEntry selected) {
        File spriteFile = selected.fetchSpriteFile();
        JPanel iconPanel = new JPanel();
        ImageIcon icon = new ImageIcon(FileUtilities.loadSprite(spriteFile));
        JLabel imageLabel = new JLabel(icon);
        iconPanel.add(imageLabel);
        return iconPanel;
    }

    @Override
    String getTooltipForEntry(Object entry) {
        if(entry instanceof HullmodCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Hullmod ID: " + checked.getHullmodID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    void showMenuIfMatching(JPopupMenu contextMenu, TreePath pathForLocation, MouseEvent e) {

    }

    @Override
    JPopupMenu getContextMenu() {
        return new JPopupMenu();
    }

    private void loadAllHullmods(Map<String, List<HullmodCSVEntry>> hullmods) {
        Map<String, HullmodCSVEntry> allHullmodEntries = SettingsManager.getAllHullmods();
        for (Map.Entry<String, List<HullmodCSVEntry>> entry : hullmods.entrySet()) {
            Path folderPath = Paths.get(entry.getKey(), "");
            DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(folderPath.getFileName().toString());

            List<HullmodCSVEntry> hullmodsInPackage = entry.getValue();
            for (HullmodCSVEntry hullmodEntry : hullmodsInPackage) {
                allHullmodEntries.putIfAbsent(hullmodEntry.getHullmodID(), hullmodEntry);
                MutableTreeNode shipNode = new DefaultMutableTreeNode(hullmodEntry);
                packageRoot.add(shipNode);
            }
            DefaultMutableTreeNode rootNode = getRootNode();
            rootNode.add(packageRoot);
        }
        log.info("Total {} hullmod entries registered.", allHullmodEntries.size());
    }

}