package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.ShipSystemsLoaded;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.utility.Pair;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 02.08.2023
 */
@Log4j2
class ShipSystemsTreePanel extends DataTreePanel{

    ShipSystemsTreePanel() {
        super("Shipsystem files");
    }

    @Override
    JPanel createTopPanel() {
        Pair<JPanel, JButton> singleButtonPanel = DataTreePanel.createSingleButtonPanel("Shipsystem data:",
                FileUtilities.getLoadShipSystemDataAction());
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload shipsystem data");
        return singleButtonPanel.getFirst();
    }

    @Override
    void initTreePanelListeners(JPanel passedTreePanel) {
        EventBus.subscribe(event -> {
            if (event instanceof ShipSystemsLoaded checked) {
                Map<String, List<ShipSystemCSVEntry>> shipsystems = checked.systemsByPackage();
                if (shipsystems == null) {
                    throw new RuntimeException("Shipsystem data initialization failed: table data is NULL!");
                }
                DefaultMutableTreeNode rootNode = getRootNode();
                rootNode.removeAllChildren();
                loadAllShipsystems(shipsystems);
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
            if (node.getUserObject() instanceof ShipSystemCSVEntry checked) {
                updateEntryPanel(checked);
            }
        });
    }

    private void updateEntryPanel(ShipSystemCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    private void loadAllShipsystems(Map<String, List<ShipSystemCSVEntry>> shipsystems) {
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, ShipSystemCSVEntry> allShipsystemEntries = gameData.getAllShipsystemEntries();
        for (Map.Entry<String, List<ShipSystemCSVEntry>> entry : shipsystems.entrySet()) {
            Path folderPath = Paths.get(entry.getKey(), "");
            DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(folderPath.getFileName().toString());

            List<ShipSystemCSVEntry> shipSystemCSVEntries = entry.getValue();
            for (ShipSystemCSVEntry shipSystemCSVEntry : shipSystemCSVEntries) {
                allShipsystemEntries.putIfAbsent(shipSystemCSVEntry.getShipSystemID(), shipSystemCSVEntry);
                MutableTreeNode shipNode = new DefaultMutableTreeNode(shipSystemCSVEntry);
                packageRoot.add(shipNode);
            }
            DefaultMutableTreeNode rootNode = getRootNode();
            rootNode.add(packageRoot);
        }
        log.info("Total {} shipsystem entries registered.", allShipsystemEntries.size());
        gameData.setShipsystemDataLoaded(true);
    }

    @Override
    String getTooltipForEntry(Object entry) {
        if(entry instanceof ShipSystemCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Shipsystem ID: " + checked.getShipSystemID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    Class<?> getEntryClass() {
        return ShipSystemCSVEntry.class;
    }

    @Override
    void openEntryPath(OpenDataTarget target) {
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (!(cachedSelectForMenu.getUserObject() instanceof ShipSystemCSVEntry checked)) return;
        Path toOpen;
        switch (target) {
            case FILE -> toOpen = checked.getTableFilePath();
            case CONTAINER -> toOpen = checked.getTableFilePath().getParent();
            default -> toOpen = checked.getPackageFolderPath();
        }
        FileUtilities.openPathInDesktop(toOpen);
    }

}
