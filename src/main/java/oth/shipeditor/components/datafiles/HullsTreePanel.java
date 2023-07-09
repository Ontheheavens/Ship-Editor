package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.communication.events.files.HullTreeCleanupQueued;
import oth.shipeditor.communication.events.files.HullTreeExpansionQueued;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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
            }
        });
    }

    private void updateEntryPanel(ShipCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 0, 5);
        JPanel shipFilesPanel = createShipFilesPanel(selected);

        rightPanel.add(shipFilesPanel, constraints);
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    private JPanel createShipFilesPanel(ShipCSVEntry selected) {
        JPanel shipFilesPanel = new JPanel();
        shipFilesPanel.setLayout(new BoxLayout(shipFilesPanel, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        Map<String, String> rowData = selected.getRowData();
        String shipName = rowData.get(StringConstants.NAME);
        String shipId = selected.getHullID();
        String hullFileName = selected.getHullFileName();
        Hull hullFile = selected.getHullFile();
        String spriteFileName = hullFile.getSpriteName();

        String skinFileName = "";
        Map<String, Skin> skins = selected.getSkins();
        JPanel rightPanel = getRightPanel();
        if (skins != null) {
            Collection<Skin> values = skins.values();
            Skin[] skinArray = values.toArray(new Skin[0]);
            JComboBox<Skin> skinChooser = new JComboBox<>(skinArray);
            skinChooser.setSelectedItem(selected.getActiveSkin());
            skinChooser.addActionListener(e -> {
                Skin chosen = (Skin) skinChooser.getSelectedItem();
                selected.setActiveSkin(chosen);
                updateEntryPanel(selected);
            });
            skinChooser.setAlignmentX(CENTER_ALIGNMENT);

            constraints.insets = new Insets(0, 0, 0, 0);
            rightPanel.add(skinChooser, constraints);
            Skin activeSkin = selected.getActiveSkin();
            if (activeSkin != null && !activeSkin.isBase()) {
                shipName = activeSkin.getHullName();
                shipId = activeSkin.getSkinHullId();
                spriteFileName = activeSkin.getSpriteName();
                skinFileName = Utility.getSkinFileName(selected, activeSkin);
            }
        } else {
            rightPanel.removeAll();
        }
        if (spriteFileName == null || spriteFileName.isEmpty()) {
            spriteFileName = hullFile.getSpriteName();
        }
        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);

        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));
        labelContainer.add(new JLabel("Ship name: " + shipName));
        labelContainer.add(new JLabel("Ship ID: " + shipId));
        labelContainer.add(new JLabel("Hull file : " + hullFileName));
        File spriteFile = new File(spriteFileName);
        labelContainer.add(new JLabel("Sprite file: : " + spriteFile.getName()));
        if (!skinFileName.isEmpty()) {
            labelContainer.add(new JLabel("Skin file: " + skinFileName));
        }
        shipFilesPanel.add(labelContainer);

        HullsTreePanel.addHullmodPanel(shipFilesPanel, selected);

        return shipFilesPanel;
    }

    private static void addHullmodPanel(JPanel panel, ShipCSVEntry selected) {
        GameDataRepository gameData = SettingsManager.getGameData();
        if (!gameData.isHullmodDataLoaded()) return;

        MatteBorder matteLine = new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY);
        Border titledBorder = new TitledBorder(matteLine, "Built-in hullmods",
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
        JPanel hullmodsPanel = new JPanel();
        hullmodsPanel.setBorder(titledBorder);
        hullmodsPanel.setAlignmentX(LEFT_ALIGNMENT);

        Collection<String> hullmodIDs = selected.getBuiltInHullmods();

        if (hullmodIDs.isEmpty()) return;

        Map<String, HullmodCSVEntry> allHullmods = gameData.getAllHullmodEntries();
        for (String id : hullmodIDs) {
            HullmodCSVEntry entry = allHullmods.get(id);
            Map<String, String> rowData = entry.getRowData();
            String name = rowData.get("name");
            BufferedImage iconImage = FileUtilities.loadSprite(entry.fetchSpriteFile());
            JLabel imageLabel = Utility.getIconLabelWithBorder(new ImageIcon(iconImage));
            imageLabel.setToolTipText(name);
            hullmodsPanel.add(imageLabel);
        }

        panel.add(hullmodsPanel);
        panel.revalidate();
        panel.repaint();
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
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel("Ship data:"));
        JButton loadCSVButton = new JButton(FileUtilities.getLoadShipDataAction());
        loadCSVButton.addActionListener(Utility.scheduleTask(3000,
                e1 -> {
                    loadCSVButton.setEnabled(false);
                    repaint();
                },
                e1 -> {
                    loadCSVButton.setEnabled(true);
                    repaint();
                }));
        loadCSVButton.setText("Reload ship data");
        loadCSVButton.setToolTipText("Reload all ship, skin and variant files, grouped by package");
        topContainer.add(loadCSVButton);
        return topContainer;
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
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        JMenuItem loadAsLayer = new JMenuItem("Load as layer");
        loadAsLayer.addActionListener(new HullsTreePanel.LoadLayerFromTree());
        menu.add(loadAsLayer);
        return menu;
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
            default -> toOpen = checked.getPackageFolder();
        }
        FileUtilities.openPathInDesktop(toOpen);
    }

}
