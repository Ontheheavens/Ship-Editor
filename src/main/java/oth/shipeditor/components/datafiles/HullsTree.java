package oth.shipeditor.components.datafiles;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.*;
import oth.shipeditor.communication.events.viewer.layers.LayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerCyclingQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    private final JTextField searchField;

    HullsTree(GameDataPanel gameDataPanel) {
        this.parent = gameDataPanel;
        hullsRoot = new DefaultMutableTreeNode("Hull files");
        hullsTree = createCustomTree();
        ToolTipManager.sharedInstance().registerComponent(hullsTree);
        JScrollPane scrollContainer = new JScrollPane(hullsTree);
        this.setLayout(new BorderLayout());
        JPanel searchContainer = new JPanel(new GridBagLayout());
        searchContainer.setBorder(new EmptyBorder(0, 0, 0, 5));
        searchField = new JTextField();
        // Set the constraints for the search field.
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0; // Allow horizontal expansion.
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // Set padding.
        // Add the search field to the container with the specified constraints.
        searchContainer.add(searchField, gridBagConstraints);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            if (query.isEmpty()) return;
            List<DefaultMutableTreeNode> nodes = getMatchingNodes(query);
            if (!nodes.isEmpty()) {
                selectMatchedNodes(nodes);
            }
        });
        searchContainer.add(searchButton);
        this.add(searchContainer, BorderLayout.PAGE_START);
        this.add(scrollContainer, BorderLayout.CENTER);
        this.initBusListening();
        this.initComponentListeners();
    }

    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    private JTree createCustomTree() {
        return new JTree(hullsRoot) {
            @Override
            public String getToolTipText(MouseEvent event) {
                if (getRowForLocation(event.getX(), event.getY()) == -1) return null;
                TreePath currPath = getPathForLocation(event.getX(), event.getY());
                if (currPath == null) return null;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) currPath.getLastPathComponent();
                Object entry = node.getUserObject();
                if(entry instanceof ShipCSVEntry checked) {
                    Hull hullFile = checked.getHullFile();
                    return "<html>" +
                            "<p>" + "Hull size: " + hullFile.getHullSize() + "</p>" +
                            "<p>" + "Hull ID: " + checked.getHullID() + "</p>" +
                            "</html>";
                }
                return null;
            }
        };
    }

    private void initBusListening() {
        EventBus.subscribe(event -> {
            if (event instanceof HullFolderWalked checked) {
                List<Map<String, String>> tableData = checked.csvData();
                if (tableData == null) return;
                loadHullList(tableData, checked.hullFiles(), checked.skinFiles(), checked.folder());
                hullsTree.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof HullTreeCleanupQueued) {
                hullsRoot.removeAllChildren();
                hullsTree.repaint();
                parent.resetInfoPanel();
                parent.repaint();
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    private List<DefaultMutableTreeNode> getMatchingNodes(String input) {
        Enumeration<TreeNode> allNodes = this.hullsRoot.depthFirstEnumeration();
        Spliterator<TreeNode> spliterator = Spliterators.spliteratorUnknownSize(
                allNodes.asIterator(), Spliterator.ORDERED);
        Stream<TreeNode> stream = StreamSupport.stream(spliterator, false);
        List<DefaultMutableTreeNode> result = stream
                .filter(node -> node instanceof DefaultMutableTreeNode)
                .map(node -> (DefaultMutableTreeNode) node)
                .filter(node -> {
                    Object userObject = node.getUserObject();
                    if (userObject == null) return false;
                    String toString = userObject.toString().toLowerCase(Locale.ROOT);
                    return toString.matches(".*" + input.toLowerCase() + ".*");
                })
                .collect(Collectors.toList());
        return result;
    }

    private void selectMatchedNodes(List<DefaultMutableTreeNode> nodes) {
            TreePath[] paths = new TreePath[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                DefaultMutableTreeNode node = nodes.get(i);
                paths[i] = new TreePath(node.getPath());
            }
            hullsTree.setSelectionPaths(paths);
            hullsTree.scrollPathToVisible(paths[0]);
    }

    @SuppressWarnings("ConstantConditions")
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

    private void loadHullList(Iterable<Map<String, String>> tableData,
                              Map<String, Hull> hullFiles, Map<String, Skin> skinFiles, Path packagePath) {
        DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(packagePath.getFileName().toString());
        for (Map<String, String> row : tableData) {
            Map.Entry<Hull, Map<String, Skin>> hullWithSkins = null;
            String fileName = "";
            for (String shipFileName : hullFiles.keySet()) {
                Hull shipFile = hullFiles.get(shipFileName);
                String hullId = shipFile.getHullId();
                if (hullId.equals(row.get("id"))) {
                    fileName = shipFileName;
                    Map<String, Skin> skins = HullsTree.fetchSkinsByHull(shipFile, skinFiles);
                    hullWithSkins = new AbstractMap.SimpleEntry<>(shipFile, skins);
                }
            }
            if (hullWithSkins != null && !fileName.isEmpty()) {
                DefaultMutableTreeNode shipNode = new DefaultMutableTreeNode(new ShipCSVEntry(row,
                        hullWithSkins, packagePath, fileName));
                packageRoot.add(shipNode);
            }
        }
        hullsRoot.add(packageRoot);
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
                Skin activeSkin = checked.getActiveSkin();
                boolean skinChosen = !activeSkin.isBase();
                if (skinChosen) {
                    spriteName = activeSkin.getSpriteName();
                    packagePath = activeSkin.getContainingPackage();
                }

                Path spriteFilePath = packagePath.resolve(spriteName);
                File spriteFile = spriteFilePath.toFile();

                EventBus.publish(new LayerCreationQueued());
                EventBus.publish(new LayerCyclingQueued());
                BufferedImage sprite = FileUtilities.loadSprite(spriteFile);
                EventBus.publish(new SpriteOpened(sprite, spriteFile.getName()));
                EventBus.publish(new HullFileOpened(hullFile, checked.getHullFileName()));
                if (skinChosen) {
                    String skinFileName = Utility.getSkinFileName(checked, activeSkin);
                    EventBus.publish(new SkinFileOpened(activeSkin, skinFileName));
                }
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
