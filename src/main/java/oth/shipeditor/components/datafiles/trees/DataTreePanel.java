package oth.shipeditor.components.datafiles.trees;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.DataTreesReloadQueued;
import oth.shipeditor.components.datafiles.OpenDataTarget;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.persistence.GameDataPackage;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
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
            topContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,  Themes.getBorderColor()));
            this.add(topContainer, BorderLayout.PAGE_START);
        }
        JPanel treePanel = createTreePanel(rootName);
        JSplitPane splitPane = createContentSplitter(treePanel);
        this.add(splitPane, BorderLayout.CENTER);
    }

    private static JLabel createVariantFileLabel(VariantFile variantFile) {
        Path variantFilePath = variantFile.getVariantFilePath();
        JLabel variantLabel = new JLabel("Variant file : " + variantFilePath.getFileName());
        variantLabel.setToolTipText(String.valueOf(variantFilePath));
        variantLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        JPopupMenu pathContextMenu = ComponentUtilities.createPathContextMenu(variantFilePath);
        variantLabel.addMouseListener(new MouseoverLabelListener(pathContextMenu, variantLabel));
        return variantLabel;
    }

    private static void outfitVariantLabelWithSelector(VariantFile variant, JPanel variantLine,
                                                       ButtonGroup group, JLabel variantFileLabel) {
        JRadioButton selector = new JRadioButton();
        selector.setBorder(new EmptyBorder(0, 0, 2, 4));
        selector.addActionListener(e -> FeaturesOverseer.setModuleForInstall(variant));
        selector.setToolTipText("Select variant or drag label to be installed as module");
        group.add(selector);
        variantLine.add(selector);

        DragSource dragSource = DragSource.getDefaultDragSource();
        DragGestureListener gestureListener = new LabelDragListener(variant, variantFileLabel);
        dragSource.createDefaultDragGestureRecognizer(variantFileLabel,
                DnDConstants.ACTION_COPY, gestureListener);
    }

    static JPanel createVariantsPanel(Collection<VariantFile> variantFiles, boolean withSelector) {
        JPanel variantsPanel = new JPanel();
        variantsPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        ComponentUtilities.outfitPanelWithTitle(variantsPanel, new Insets(1, 0, 0, 0), "Variants");
        variantsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(2, 0, 0, 0));
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));

        if (variantFiles.isEmpty()) throw new IllegalArgumentException("Empty variants list!");

        ButtonGroup group = new ButtonGroup();
        variantFiles.forEach(variant -> {
            JPanel variantLine = new JPanel();
            variantLine.setAlignmentX(LEFT_ALIGNMENT);
            variantLine.setLayout(new BoxLayout(variantLine, BoxLayout.LINE_AXIS));

            JLabel variantFileLabel = DataTreePanel.createVariantFileLabel(variant);
            if (withSelector) {
                DataTreePanel.outfitVariantLabelWithSelector(variant, variantLine,
                        group, variantFileLabel);
            }
            variantLine.add(variantFileLabel);
            labelContainer.add(variantLine);
            labelContainer.add(Box.createVerticalStrut(2));
        });

        if (withSelector) {
            Enumeration<AbstractButton> elements = group.getElements();
            AbstractButton abstractButton = elements.nextElement();
            abstractButton.doClick();
        }

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

    void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    void resetInfoPanel() {
        rightPanel.removeAll();
        rightPanel.add(new JLabel(StringValues.NO_ENTRY_SELECTED));
        rightPanel.revalidate();
        rightPanel.repaint();
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
        float resizeWeight = getSplitterResizeWeight();
        treeSplitter.setResizeWeight(resizeWeight);
        treeSplitter.setLeftComponent(treeContainer);
        treeSplitter.setRightComponent(rightPanel);
        return treeSplitter;
    }

    protected float getSplitterResizeWeight() {
        return 0.4f;
    }

    private JPanel createTreePanel(String rootName) {
        JPanel createdTreePanel = new JPanel();
        rootNode = new DefaultMutableTreeNode(rootName);
        tree = createCustomTree();
        ToolTipManager.sharedInstance().registerComponent(tree);
        JScrollPane scrollContainer = new JScrollPane(tree);
        createdTreePanel.setLayout(new BorderLayout());
        JPanel searchContainer = createSearchContainer();
        createdTreePanel.add(searchContainer, BorderLayout.PAGE_START);
        createdTreePanel.add(scrollContainer, BorderLayout.CENTER);
        this.initTreePanelListeners(createdTreePanel);
        return createdTreePanel;
    }

    @SuppressWarnings("WeakerAccess")
    protected JPanel createSearchContainer() {
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
        return searchContainer;
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

    DefaultMutableTreeNode getNodeOfEntry(CSVEntry entry) {
        DefaultMutableTreeNode root = this.getRootNode();
        Enumeration<TreeNode> allNodes = root.depthFirstEnumeration();
        Spliterator<TreeNode> spliterator = Spliterators.spliteratorUnknownSize(
                allNodes.asIterator(), Spliterator.ORDERED);
        Stream<TreeNode> stream = StreamSupport.stream(spliterator, false);
        Optional<DefaultMutableTreeNode> treeNode = stream
                .filter(node -> node instanceof DefaultMutableTreeNode)
                .map(node -> (DefaultMutableTreeNode) node)
                .filter(node -> {
                    Object userObject = node.getUserObject();
                    if (userObject instanceof CSVEntry csvEntry) {
                        String entryID = csvEntry.getID();
                        return entryID.equals(entry.getID());
                    }
                    return false;
                }).findFirst();
        return treeNode.orElse(null);
    }

    JTree createCustomTree() {
        JTree customTree = new JTree(getRootNode()) {
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
        DragSource dragSource = DragSource.getDefaultDragSource();
        DragGestureListener gestureListener = new TreeDataGestureListener(customTree);
        dragSource.createDefaultDragGestureRecognizer(customTree, DnDConstants.ACTION_COPY,
                gestureListener);
        return customTree;
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

    public abstract void reload();

    void sortAndExpandTree() {
        Enumeration<TreeNode> children = rootNode.children();
        List<DefaultMutableTreeNode> nodeList = new ArrayList<>();

        while (children.hasMoreElements()) {
            TreeNode folder = children.nextElement();
            if (folder instanceof DefaultMutableTreeNode packageNode) {
                nodeList.add(packageNode);
            }
        }

        nodeList.sort((firstNode, secondNode) -> {
            Object firstNodeUserObject = firstNode.getUserObject();
            GameDataPackage firstDataPackage = (GameDataPackage) firstNodeUserObject;
            Object secondNodeUserObject = secondNode.getUserObject();
            GameDataPackage secondDataPackage = (GameDataPackage) secondNodeUserObject;

            if (SettingsManager.isCoreFolder(firstDataPackage)) {
                return -1;
            }
            else if (SettingsManager.isCoreFolder(secondDataPackage)) {
                return 1;
            }

            if (firstDataPackage.isPinned() && !secondDataPackage.isPinned()) {
                return -1;
            }
            else if (!firstDataPackage.isPinned() && secondDataPackage.isPinned()) {
                return 1;
            }

            String firstFolderName = firstDataPackage.getFolderName();
            String secondFolderName = secondDataPackage.getFolderName();
            return firstFolderName.compareToIgnoreCase(secondFolderName);
        });

        rootNode.removeAllChildren();
        for (DefaultMutableTreeNode packageNode : nodeList) {
            rootNode.add(packageNode);
        }

        Enumeration<TreeNode> updatedPackages = rootNode.children();
        while (updatedPackages.hasMoreElements()) {
            TreeNode folder = updatedPackages.nextElement();
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

    static String getTooltipForPackage(GameDataPackage dataPackage) {
        String corePackageLine = "";
        boolean isCoreFolder = SettingsManager.isCoreFolder(dataPackage);
        if (isCoreFolder) {
            corePackageLine = "<p>" + "Is a core package" + "</p>";
        }
        String pinnedPackageLine = "";
        boolean isPinned = dataPackage.isPinned();
        if (isPinned) {
            pinnedPackageLine = "<p>" + "Is pinned" + "</p>";
        }
        if (isCoreFolder || isPinned) {
            return "<html>" + corePackageLine + pinnedPackageLine + "</html>";
        }
        return null;
    }

    void createRightPanelDataTable(CSVEntry entry) {
        Map<String, String> data = entry.getRowData();
        JScrollPane tableContainer = DataTreePanel.createTableFromMap(data);
        this.addContentToRightPanel(tableContainer, entry);
    }

    private void addContentToRightPanel(JComponent component, CSVEntry entry) {
        GridBagConstraints otherConstraints = new GridBagConstraints();
        otherConstraints.gridx = 0;
        otherConstraints.gridy = 2;
        otherConstraints.fill = GridBagConstraints.BOTH;
        otherConstraints.weightx = 1.0;
        otherConstraints.weighty = 1.0;
        otherConstraints.insets = new Insets(0, 0, 0, 0);

        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new BorderLayout());

        JPanel buttonsContainer = DataTreePanel.createTableButtons(entry);

        ComponentUtilities.outfitPanelWithTitle(buttonsContainer,
                new Insets(1, 0, 0, 0), "CSV Data");

        tableContainer.add(component, BorderLayout.CENTER);
        tableContainer.add(buttonsContainer, BorderLayout.PAGE_START);

        rightPanel.add(tableContainer, otherConstraints);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private static JPanel createTableButtons(CSVEntry entry) {
        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setLayout(new GridLayout(1, 2));

        JButton openTableButton = new JButton("Open table");
        openTableButton.addActionListener(e -> {
            Path toOpen = entry.getTableFilePath();
            FileUtilities.openPathInDesktop(toOpen);
        });
        buttonsContainer.add(openTableButton);

        JButton openFolderButton = new JButton("Open folder");
        openFolderButton.addActionListener(e -> {
            Path toOpen = entry.getTableFilePath().getParent();
            FileUtilities.openPathInDesktop(toOpen);
        });
        buttonsContainer.add(openFolderButton);
        return buttonsContainer;
    }

    static void configureCellRendererColors(Object userObject, JLabel stamp) {
        Color textColor = Themes.getTextColor();
        stamp.setForeground(textColor);
        if (userObject instanceof GameDataPackage dataPackage) {
            stamp.setText(dataPackage.getFolderName());
            if (SettingsManager.isCoreFolder(dataPackage)) {
                stamp.setForeground(Themes.getCorePackageTextColor());
            } else if (dataPackage.isPinned()) {
                stamp.setForeground(Themes.getPinnedPackageTextColor());
            }
        }
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
            Object userObject = cachedSelectForMenu.getUserObject();
            if (entryClass.isInstance(userObject)) {
                tree.setSelectionPath(pathForLocation);
                contextMenu.show(tree, e.getPoint().x, e.getPoint().y);
            } else if (userObject instanceof GameDataPackage dataPackage && !SettingsManager.isCoreFolder(dataPackage)) {
                JPopupMenu menu = new JPopupMenu();

                if (dataPackage.isPinned()) {
                    JMenuItem unpinPackage = new JMenuItem("Unpin package");
                    unpinPackage.addActionListener(event -> {
                        dataPackage.setPinned(false);
                        SettingsManager.updateFileFromRuntime();
                        EventBus.publish(new DataTreesReloadQueued());
                    });
                    menu.add(unpinPackage);
                } else {
                    JMenuItem pinPackage = new JMenuItem("Pin package");
                    pinPackage.addActionListener(event -> {
                        dataPackage.setPinned(true);
                        SettingsManager.updateFileFromRuntime();
                        EventBus.publish(new DataTreesReloadQueued());
                    });
                    menu.add(pinPackage);
                }

                JMenuItem disablePackage = new JMenuItem("Disable package");
                disablePackage.addActionListener(event -> {
                    dataPackage.setDisabled(true);
                    SettingsManager.updateFileFromRuntime();
                    EventBus.publish(new DataTreesReloadQueued());
                });
                menu.add(disablePackage);

                menu.show(tree, e.getPoint().x, e.getPoint().y);
            }
        }

        private JPopupMenu createContextMenu() {
            return getContextMenu();
        }

    }


}
