package oth.shipeditor.components.datafiles.trees;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.OpenDataTarget;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.Variant;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@SuppressWarnings("ClassWithTooManyMethods")
@Log4j2
public abstract class DataTreePanel extends JPanel {

    @Getter
    private DefaultMutableTreeNode rootNode;

    @Getter
    private DefaultMutableTreeNode cachedSelectForMenu;

    @Getter
    private JTree tree;

    private JTextField searchField;

    @Getter
    private JPanel rightPanel;

    protected DataTreePanel(String rootName) {
        this.setLayout(new BorderLayout());
        JPanel topContainer = createTopPanel();
        if (topContainer != null) {
            topContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            this.add(topContainer, BorderLayout.PAGE_START);
        }
        JPanel treePanel = createTreePanel(rootName);
        JSplitPane splitPane = createContentSplitter(treePanel);
        this.add(splitPane, BorderLayout.CENTER);
    }

    private static JLabel createVariantFileLabel(Variant variant) {
        Path variantFilePath = variant.getVariantFilePath();
        JLabel variantLabel = new JLabel("Variant file : " + variantFilePath.getFileName());
        variantLabel.setToolTipText(String.valueOf(variantFilePath));
        variantLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        JPopupMenu pathContextMenu = ComponentUtilities.createPathContextMenu(variantFilePath);
        variantLabel.addMouseListener(new MouseoverLabelListener(pathContextMenu, variantLabel));
        return variantLabel;
    }

    static JPanel createVariantsPanel(Collection<Variant> variants, Dimension pad) {
        JPanel variantsPanel = new JPanel();
        variantsPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        ComponentUtilities.outfitPanelWithTitle(variantsPanel, new Insets(1, 0, 0, 0), "Variants");
        variantsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(2, 0, 0, 0));
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));

        if (variants.isEmpty()) throw new IllegalArgumentException("Empty variants list!");
        variants.forEach(variant -> {
            labelContainer.add(DataTreePanel.createVariantFileLabel(variant));
            labelContainer.add(Box.createRigidArea(pad));
        });

        variantsPanel.add(labelContainer);
        return variantsPanel;
    }

    static boolean isCurrentSkinNotEligible() {
        var activeLayer = StaticController.getActiveLayer();
        var isShipLayer = activeLayer instanceof ShipLayer;
        ShipLayer shipLayer;
        if (isShipLayer) {
            shipLayer = (ShipLayer) activeLayer;
        } else return true;

        ShipPainter shipPainter = shipLayer.getPainter();
        if (shipPainter == null || shipPainter.isUninitialized()) return true;
        var skin = shipPainter.getActiveSkin();
        return skin == null || skin.isBase();
    }

    protected abstract JPanel createTopPanel();

    void resetInfoPanel() {
        rightPanel.removeAll();
        rightPanel.add(new JLabel(StringValues.NO_ENTRY_SELECTED));
    }

    private static JScrollPane createTableFromMap(Map<String, String> data) {
        Set<Map.Entry<String, String>> entries = data.entrySet();
        Object[][] tableData = entries.stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toArray(Object[][]::new);
        DefaultTableModel model = new DefaultTableModel(tableData, new Object[]{"Property", "Value"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table read-only.
            }
        };
        JTable table = new JTable(model) {
            public String getToolTipText(MouseEvent event) {
                String tip = null;
                Point p = event.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    Object valueAt = getValueAt(rowIndex, colIndex);
                    tip = valueAt.toString();
                } catch (RuntimeException ignored) {}
                return tip;
            }
        };
        return new JScrollPane(table);
    }

    private JSplitPane createContentSplitter(JPanel treeContainer) {
        rightPanel = new JPanel(new GridBagLayout());
        rightPanel.add(new JLabel(StringValues.NO_ENTRY_SELECTED));
        JSplitPane treeSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeSplitter.setOneTouchExpandable(true);
        treeSplitter.setResizeWeight(0.4f);
        treeSplitter.setLeftComponent(treeContainer);
        treeSplitter.setRightComponent(rightPanel);
        return treeSplitter;
    }

    private JPanel createTreePanel(String rootName) {
        JPanel createdTreePanel = new JPanel();
        rootNode = new DefaultMutableTreeNode(rootName);
        tree = createCustomTree();
        ToolTipManager.sharedInstance().registerComponent(tree);
        JScrollPane scrollContainer = new JScrollPane(tree);
        createdTreePanel.setLayout(new BorderLayout());
        JPanel searchContainer = new JPanel(new GridBagLayout());
        searchContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
        searchField = new JTextField();
        // Set the constraints for the search field.
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0; // Allow horizontal expansion.
        gridBagConstraints.insets = new Insets(0, 0, 0, 0); // Set padding.
        // Add the search field to the container with the specified constraints.
        searchContainer.add(searchField, gridBagConstraints);
        JButton searchButton = new JButton(StringValues.SEARCH);
        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            if (query.isEmpty()) return;
            List<DefaultMutableTreeNode> nodes = getMatchingNodes(query);
            if (!nodes.isEmpty()) {
                selectMatchedNodes(nodes);
            }
        });
        searchContainer.add(searchButton);
        createdTreePanel.add(searchContainer, BorderLayout.PAGE_START);
        createdTreePanel.add(scrollContainer, BorderLayout.CENTER);
        this.initTreePanelListeners(createdTreePanel);
        return createdTreePanel;
    }

    static GridBagConstraints getDefaultConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(0, 0, 0, 0);
        return constraints;
    }

    protected abstract void initTreePanelListeners(JPanel passedTreePanel);

    JTree createCustomTree() {
        return new JTree(getRootNode()) {
            @Override
            public String getToolTipText(MouseEvent event) {
                if (getRowForLocation(event.getX(), event.getY()) == -1) return null;
                TreePath currPath = getPathForLocation(event.getX(), event.getY());
                if (currPath == null) return null;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) currPath.getLastPathComponent();
                Object entry = node.getUserObject();
                return getTooltipForEntry(entry);
            }
        };
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private List<DefaultMutableTreeNode> getMatchingNodes(String input) {
        Enumeration<TreeNode> allNodes = this.rootNode.depthFirstEnumeration();
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
        tree.setSelectionPaths(paths);
        tree.scrollPathToVisible(paths[0]);
    }

    void sortAndExpandTree() {
        Enumeration<TreeNode> children = rootNode.children();
        while (children.hasMoreElements()) {
            TreeNode folder = children.nextElement();
            DataTreePanel.sortFolderNode(folder, (node1, node2) -> {
                String name1 = node1.toString();
                String name2 = node2.toString();
                return name1.compareToIgnoreCase(name2);
            });
        }
        if (tree.getModel() instanceof DefaultTreeModel checked) {
            checked.nodeStructureChanged(rootNode);
        }
        tree.expandPath(new TreePath(rootNode));
        tree.repaint();
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

    void createRightPanelDataTable(Map<String, String> data) {
        JScrollPane tableContainer = DataTreePanel.createTableFromMap(data);
        this.addContentToRightPanel(tableContainer);
    }

    private void addContentToRightPanel(JComponent component) {
        GridBagConstraints otherConstraints = new GridBagConstraints();
        otherConstraints.gridx = 0;
        otherConstraints.gridy = 2;
        otherConstraints.fill = GridBagConstraints.BOTH;
        otherConstraints.weightx = 1.0;
        otherConstraints.weighty = 1.0;
        otherConstraints.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(component, otherConstraints);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    protected abstract String getTooltipForEntry(Object entry);

    protected abstract Class<?> getEntryClass();

    JPopupMenu getContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem collapsePackage = new JMenuItem("Collapse package");
        collapsePackage.addActionListener(getCollapseAction());
        menu.add(collapsePackage);
        menu.addSeparator();
        JMenuItem openSourceFile = new JMenuItem(StringValues.OPEN_SOURCE_FILE);
        openSourceFile.addActionListener(e -> openEntryPath(OpenDataTarget.FILE));
        menu.add(openSourceFile);
        JMenuItem openInExplorer = new JMenuItem(StringValues.OPEN_CONTAINING_FOLDER);
        openInExplorer.addActionListener(e -> openEntryPath(OpenDataTarget.CONTAINER));
        menu.add(openInExplorer);
        JMenuItem openPackage = new JMenuItem(StringValues.OPEN_DATA_PACKAGE);
        openPackage.addActionListener(e -> openEntryPath(OpenDataTarget.PACKAGE));
        menu.add(openPackage);
        return menu;
    }

    protected abstract void openEntryPath(OpenDataTarget target);
    
    private ActionListener getCollapseAction() {
        return e -> {
            Class<?> entryClass = getEntryClass();
            if (entryClass.isInstance(this.cachedSelectForMenu.getUserObject())) {
                TreePath selected = this.tree.getSelectionPath();
                if (selected != null) {
                    this.tree.collapsePath(selected.getParentPath());
                }
            }
        };
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    class ContextMenuListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3){
                TreePath pathForLocation = tree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                if(pathForLocation != null){
                    cachedSelectForMenu = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                    JPopupMenu contextMenu = createContextMenu();
                    showMenuIfMatching(contextMenu, pathForLocation, e);
                } else{
                    cachedSelectForMenu = null;
                }
            }
            super.mousePressed(e);
        }

        private void showMenuIfMatching(JPopupMenu contextMenu, TreePath pathForLocation, MouseEvent e) {
            Class<?> entryClass = getEntryClass();
            if (entryClass.isInstance(cachedSelectForMenu.getUserObject())) {
                tree.setSelectionPath(pathForLocation);
                contextMenu.show(tree, e.getPoint().x, e.getPoint().y);
            }
        }

        private JPopupMenu createContextMenu() {
            return getContextMenu();
        }

    }


}
