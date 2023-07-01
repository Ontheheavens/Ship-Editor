package oth.shipeditor.components.datafiles;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.communication.events.files.HullTreeExpansionQueued;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.LayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerCyclingQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.Files;
import oth.shipeditor.representation.Hull;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
class HullsTree extends JPanel {

    private final DefaultMutableTreeNode hullsRoot;

    private DefaultMutableTreeNode cachedSelectForMenu;

    @Getter
    private final JTree hullsTree;

    private final GameDataPanel parent;

    HullsTree(GameDataPanel gameDataPanel) {
        this.parent = gameDataPanel;
        hullsRoot = new DefaultMutableTreeNode("Hull files");
        hullsTree = new JTree(hullsRoot);
        JScrollPane scrollContainer = new JScrollPane(hullsTree);
        this.setLayout(new BorderLayout());
        this.add(scrollContainer);
        this.initBusListening();
        this.initComponentListeners();
    }

    private void initBusListening() {
        EventBus.subscribe(event -> {
            if (event instanceof HullFolderWalked checked) {
                List<Map<String, String>> tableData = checked.csvData();
                if (tableData == null) return;
                loadHullList(tableData, checked.hullFiles(), checked.folder());
                hullsTree.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof HullTreeExpansionQueued) {
                Enumeration<TreeNode> children = hullsRoot.children();
                while (children.hasMoreElements()) {
                    TreeNode folder = children.nextElement();
                    HullsTree.sortFolderNode(folder, (node1, node2) -> {
                        String name1 = node1.toString();
                        String name2 = node2.toString();
                        return name1.compareToIgnoreCase(name2);
                    });
                }
                if (hullsTree.getModel() instanceof DefaultTreeModel checked) {
                    checked.nodeStructureChanged(hullsRoot);
                }
                hullsTree.expandPath(new TreePath(hullsRoot));
                hullsTree.repaint();
            }
        });
    }

    private static void sortFolderNode(TreeNode folder, Comparator<DefaultMutableTreeNode> comparator) {
        Enumeration<? extends TreeNode> children = folder.children();
        List<DefaultMutableTreeNode> nodeList = new ArrayList<>();
        while (children.hasMoreElements()) {
            if (children.nextElement() instanceof DefaultMutableTreeNode checked) {
                nodeList.add(checked);
            }
        }
        nodeList.sort(comparator);
        DefaultMutableTreeNode casted = (DefaultMutableTreeNode) folder;
        casted.removeAllChildren();
        for (MutableTreeNode node : nodeList) {
            casted.add(node);
        }
    }

    private void initComponentListeners() {
        hullsTree.addMouseListener(new ContextMenuListener());
        hullsTree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            if (selectedNode == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            if (node.getUserObject() instanceof ShipCSVEntry checked) {
                parent.updateEntryPanel(checked);
            }
        });
    }

    private DefaultMutableTreeNode loadHullList(Iterable<Map<String, String>> tableData,
                                                Map<String, Hull> hullFiles, Path packagePath) {
        DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(packagePath.getFileName().toString());
        for (Map<String, String> row : tableData) {
            Hull matching = null;
            String fileName = "";
            for (String shipFileName : hullFiles.keySet()) {
                Hull shipFile = hullFiles.get(shipFileName);
                String hullId = shipFile.getHullId();
                if (hullId.equals(row.get("id"))) {
                    matching = shipFile;
                    fileName = shipFileName;
                }
            }
            if (matching != null && !fileName.isEmpty()) {
                MutableTreeNode shipNode = new DefaultMutableTreeNode(new ShipCSVEntry(row,
                        matching, packagePath, fileName));
                packageRoot.add(shipNode);
            }
        }
        hullsRoot.add(packageRoot);
        return packageRoot;
    }

    private class LoadLayerFromTree extends AbstractAction {
        @Override
        public boolean isEnabled() {
            return super.isEnabled() && cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry checked) {
                Path packagePath = checked.getPackageFolder();
                Hull hullFile = checked.getHullFile();
                String spriteName = hullFile.getSpriteName();

                Path spriteFilePath = packagePath.resolve(spriteName);
                File spriteFile = spriteFilePath.toFile();

                EventBus.publish(new LayerCreationQueued());
                EventBus.publish(new LayerCyclingQueued());
                BufferedImage sprite = Files.loadSprite(spriteFile);
                EventBus.publish(new SpriteOpened(sprite, spriteFile.getName()));
                EventBus.publish(new HullFileOpened(hullFile, checked.getHullFileName()));
            }
        }

    }

    private class ContextMenuListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3){
                TreePath pathForLocation = hullsTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                if(pathForLocation != null){
                    cachedSelectForMenu = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                    JPopupMenu contextMenu = createContextMenu();
                    if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry) {
                        hullsTree.setSelectionPath(pathForLocation);
                        contextMenu.show(hullsTree, e.getPoint().x, e.getPoint().y);
                    }
                } else{
                    cachedSelectForMenu = null;
                }
            }
            super.mousePressed(e);
        }

        private JPopupMenu createContextMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem loadAsLayer = new JMenuItem("Load as layer");
            loadAsLayer.addActionListener(new LoadLayerFromTree());
            menu.add(loadAsLayer);
            JMenuItem collapsePackage = new JMenuItem("Collapse parent");
            collapsePackage.addActionListener(e -> {
                if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry) {
                    TreePath selected = hullsTree.getSelectionPath();
                    if (selected != null) {
                        hullsTree.collapsePath(selected.getParentPath());
                    }
                }
            });
            menu.add(collapsePackage);
            return menu;
        }

    }

}
