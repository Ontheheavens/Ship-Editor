package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.OpenDataTarget;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Ontheheavens
 * @since 04.08.2023
 */
@Log4j2
public abstract class CSVDataTreePanel<T extends CSVEntry> extends DataTreePanel {

    CSVDataTreePanel(String rootName) {
        super(rootName);
    }

    protected abstract Action getLoadDataAction();

    protected abstract String getEntryTypeName();

    @SuppressWarnings("unused")
    @Override
    protected JPanel createTopPanel() {
        String entryTypeName = getEntryTypeName();
        String capitalized = Utility.capitalizeFirstLetter(entryTypeName);
        Pair<JPanel, JButton> singleButtonPanel = ComponentUtilities.createSingleButtonPanel("",
                getLoadDataAction());
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload " + entryTypeName + " data");
        button.setToolTipText("Reload all " + entryTypeName + " entries, grouped by package");
        return singleButtonPanel.getFirst();
    }

    void populateEntries(Map<String, List<T>> entriesByPackage) {
        DefaultMutableTreeNode rootNode = getRootNode();
        rootNode.removeAllChildren();
        loadAllEntries(entriesByPackage);
        sortAndExpandTree();
        repaint();
    }

    DefaultMutableTreeNode getNodeOfEntry(CSVEntry entry) {
        DefaultMutableTreeNode rootNode = this.getRootNode();
        Enumeration<TreeNode> allNodes = rootNode.depthFirstEnumeration();
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

    protected abstract Map<String, T> getRepository();

    protected abstract void setLoadedStatus();

    protected void loadAllEntries(Map<String, List<T>> entries) {
        Map<String, T> entriesRepository = getRepository();
        for (Map.Entry<String, List<T>> entry : entries.entrySet()) {
            Path folderPath = Paths.get(entry.getKey(), "");
            DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(folderPath.getFileName().toString());

            List<T> entriesInPackage = entry.getValue();
            for (T dataEntry : entriesInPackage) {
                entriesRepository.putIfAbsent(dataEntry.getID(), dataEntry);
                MutableTreeNode node = new DefaultMutableTreeNode(dataEntry);
                packageRoot.add(node);
            }
            DefaultMutableTreeNode rootNode = getRootNode();
            rootNode.add(packageRoot);
        }
        log.info("Total {} {} entries registered.", entriesRepository.size(), getEntryTypeName());
        setLoadedStatus();
    }

    @Override
    protected void initTreePanelListeners(JPanel passedTreePanel) {
        initComponentListeners();
        initWalkerListening();
    }

    protected abstract void initWalkerListening();

    private void initComponentListeners() {
        JTree tree = getTree();
        tree.addMouseListener(new ContextMenuListener());
        tree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            if (selectedNode == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            T entryObject = getObjectFromNode(node);
            if (entryObject != null) {
                updateEntryPanel(entryObject);
            }
        });
    }

    protected abstract void updateEntryPanel(T selected);

    protected abstract T getObjectFromNode(DefaultMutableTreeNode node);

    @Override
    protected void openEntryPath(OpenDataTarget target) {
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        T entryObject = getObjectFromNode(cachedSelectForMenu);
        if (entryObject == null) return;
        Path toOpen;
        switch (target) {
            case FILE -> toOpen = entryObject.getTableFilePath();
            case CONTAINER -> toOpen = entryObject.getTableFilePath().getParent();
            default -> toOpen = entryObject.getPackageFolderPath();
        }
        FileUtilities.openPathInDesktop(toOpen);
    }

}
