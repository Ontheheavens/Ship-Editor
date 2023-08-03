package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.GameDataPanelResized;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.communication.events.files.HullTreeCleanupQueued;
import oth.shipeditor.communication.events.files.HullTreeExpansionQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.Pair;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
class HullsTreePanel extends DataTreePanel {

    HullsTreePanel() {
        super("Hull files");
    }

    @Override
    String getTooltipForEntry(Object entry) {
        if(entry instanceof ShipCSVEntry checked) {
            Hull hullFile = checked.getHullFile();
            return "<html>" +
                    "<p>" + "Hull size: " + hullFile.getHullSize() + "</p>" +
                    "<p>" + "Hull ID: " + checked.getHullID() + "</p>" +
                    "<p>" + "(Double-click to load as layer)" + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    void initTreePanelListeners(JPanel passedTreePanel) {
        this.initBusListening();
        this.initComponentListeners();
    }

    private void initBusListening() {
        JTree tree = getTree();
        DefaultMutableTreeNode rootNode = getRootNode();
        EventBus.subscribe(event -> {
            if (event instanceof HullFolderWalked checked) {
                List<Map<String, String>> tableData = checked.csvData();
                if (tableData == null) {
                    throw new RuntimeException("Ship data initialization failed: table data is NULL!");
                }
                loadHullList(tableData, checked.hullFiles(), checked.skinFiles(), checked.folder());
                tree.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof HullTreeCleanupQueued) {
                rootNode.removeAllChildren();
                tree.repaint();
                resetInfoPanel();
                repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof HullTreeExpansionQueued) {
                sortAndExpandTree();
            }
        });
    }

    private void initComponentListeners() {
        JTree tree = getTree();
        tree.addMouseListener(new ContextMenuListener());
        tree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            if (selectedNode == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            if (node.getUserObject() instanceof ShipCSVEntry checked) {
                updateEntryPanel(checked);
                EventBus.publish(new GameDataPanelResized(this.getMinimumSize()));
            }
        });
        tree.addMouseListener(new DoubleClickLayerLoader());
    }

    void updateEntryPanel(ShipCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 0, 5);
        ShipFilesSubpanel shipFilesSubpanel = new ShipFilesSubpanel(rightPanel);
        JPanel shipFilesPanel = shipFilesSubpanel.createShipFilesPanel(selected, this);

        rightPanel.add(shipFilesPanel, constraints);
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    private void loadHullList(Iterable<Map<String, String>> tableData,
                              Map<String, Hull> hullFiles, Map<String, Skin> skinFiles, Path packagePath) {
        DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(packagePath.getFileName().toString());
        for (Map<String, String> row : tableData) {
            Map.Entry<Hull, Map<String, Skin>> hullWithSkins = null;
            String fileName = "";
            String rowId = row.get("id");
            for (String shipFileName : hullFiles.keySet()) {
                Hull shipFile = hullFiles.get(shipFileName);
                String hullId = shipFile.getHullId();
                if (hullId.equals(rowId)) {
                    fileName = shipFileName;
                    Map<String, Skin> skins = HullsTreePanel.fetchSkinsByHull(shipFile, skinFiles);
                    hullWithSkins = new AbstractMap.SimpleEntry<>(shipFile, skins);
                }
            }
            if (hullWithSkins != null && !fileName.isEmpty()) {
                ShipCSVEntry newEntry = new ShipCSVEntry(row, hullWithSkins, packagePath, fileName);
                GameDataRepository gameData = SettingsManager.getGameData();
                Map<String, ShipCSVEntry> allShipEntries = gameData.getAllShipEntries();
                allShipEntries.putIfAbsent(rowId, newEntry);
                MutableTreeNode shipNode = new DefaultMutableTreeNode(newEntry);
                packageRoot.add(shipNode);
            }
        }
        DefaultMutableTreeNode rootNode = getRootNode();
        rootNode.add(packageRoot);
    }

    private static Map<String, Skin> fetchSkinsByHull(Hull hull, Map<String, Skin> skins) {
        if (skins == null) return null;
        String hullId = hull.getHullId();
        Map<String, Skin> associated = new HashMap<>();
        for (Map.Entry<String, Skin> skin : skins.entrySet()) {
            Skin value = skin.getValue();
            if (Objects.equals(value.getBaseHullId(), hullId)) {
                associated.put(skin.getKey(), skin.getValue());
            }
        }
        if (!associated.isEmpty()) {
            return associated;
        }
        return null;
    }

    @Override
    JPanel createTopPanel() {
        Pair<JPanel, JButton> singleButtonPanel = DataTreePanel.createSingleButtonPanel("Ship data:",
                FileUtilities.getLoadShipDataAction());
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload ship data");
        button.setToolTipText("Reload all ship, skin and variant files, grouped by package");

        return singleButtonPanel.getFirst();
    }

    private class LoadLayerFromTree extends AbstractAction {
        @Override
        public boolean isEnabled() {
            DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
            return super.isEnabled() && cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
            if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry checked) {
                checked.loadLayerFromEntry();
            }
        }
    }

    @Override
    Class<?> getEntryClass() {
        return ShipCSVEntry.class;
    }

    @Override
    protected JTree createCustomTree() {
        JTree custom = super.createCustomTree();
        custom.setCellRenderer(new HullsTreeCellRenderer());
        return custom;
    }
    @Override
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry checked) {
            JMenuItem openSkin = HullsTreePanel.addOpenSkinOption(checked);
            if (openSkin != null) {
                menu.add(openSkin);
            }
        }
        menu.addSeparator();
        JMenuItem loadAsLayer = new JMenuItem("Load as layer");
        loadAsLayer.addActionListener(new HullsTreePanel.LoadLayerFromTree());
        menu.add(loadAsLayer);
        return menu;
    }

    private static JMenuItem addOpenSkinOption(ShipCSVEntry checked) {
        Skin activeSkin = checked.getActiveSkin();
        if (activeSkin == null || activeSkin.isBase()) return null;
        JMenuItem openSkin = new JMenuItem("Open skin file");
        openSkin.addActionListener(e -> {
            Path toOpen = activeSkin.getSkinFilePath();
            FileUtilities.openPathInDesktop(toOpen);
        });
        return openSkin;
    }

    @Override
    void openEntryPath(OpenDataTarget target) {
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (!(cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry checked)) return;
        Hull hullFile = checked.getHullFile();
        Path toOpen;
        switch (target) {
            case FILE -> toOpen = hullFile.getShipFilePath();
            case CONTAINER -> toOpen = hullFile.getShipFilePath().getParent();
            default -> toOpen = checked.getPackageFolderPath();
        }
        FileUtilities.openPathInDesktop(toOpen);
    }

    private static class HullsTreeCellRenderer extends DefaultTreeCellRenderer {

        @SuppressWarnings("ParameterHidesMemberVariable")
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            if (object instanceof ShipCSVEntry checked && leaf) {
                Hull hull = checked.getHullFile();
                String hullSize = hull.getHullSize();
                switch (hullSize) {
                    case "FIGHTER" -> setIcon(FontIcon.of(BoxiconsRegular.DICE_1, 16, Color.DARK_GRAY));
                    case "FRIGATE" -> setIcon(FontIcon.of(BoxiconsRegular.DICE_2, 16, Color.DARK_GRAY));
                    case "DESTROYER" -> setIcon(FontIcon.of(BoxiconsRegular.DICE_3, 16, Color.DARK_GRAY));
                    case "CRUISER" -> setIcon(FontIcon.of(BoxiconsRegular.DICE_4, 16, Color.DARK_GRAY));
                    case "CAPITAL_SHIP" -> setIcon(FontIcon.of(BoxiconsRegular.DICE_5, 16, Color.DARK_GRAY));
                    default -> {}
                }
            }

            return this;
        }

    }

    private class DoubleClickLayerLoader extends MouseAdapter {

        @SuppressWarnings("ChainOfInstanceofChecks")
        @Override
        public void mouseClicked(MouseEvent e) {
            // Check for double-click.
            if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() < 2) return;
            JTree tree = getTree();
            Point eventPoint = e.getPoint();
            TreePath pathForLocation = tree.getPathForLocation(eventPoint.x, eventPoint.y);
            if (pathForLocation == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath == null) return;
            Object selected = selectionPath.getLastPathComponent();
            if (node == null || !(selected instanceof DefaultMutableTreeNode checkedNode) || node != checkedNode) return;
            if (node.getUserObject() instanceof ShipCSVEntry checked) {
                checked.loadLayerFromEntry();
            }
        }
    }

}
