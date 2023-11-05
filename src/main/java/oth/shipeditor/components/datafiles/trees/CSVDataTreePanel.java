package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.OpenDataTarget;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.persistence.GameDataPackage;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    protected JTree createCustomTree() {
        JTree custom = super.createCustomTree();
        custom.setCellRenderer(new CSVDataCellRenderer());
        return custom;
    }

    private static class CSVDataCellRenderer extends DefaultTreeCellRenderer {

        @SuppressWarnings("ParameterHidesMemberVariable")
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            DataTreePanel.configureCellRendererColors(object, this);
            return this;
        }

    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if (entry instanceof GameDataPackage dataPackage) {
            return DataTreePanel.getTooltipForPackage(dataPackage);
        }
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    protected JPanel createTopPanel() {
        String entryTypeName = getEntryTypeName();
        String capitalized = Utility.capitalizeFirstLetter(entryTypeName);
        Pair<JPanel, JButton> singleButtonPanel = ComponentUtilities.createLoaderButtonPanel("",
                getLoadDataAction());
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload " + entryTypeName + " data");
        button.setToolTipText("Reload all " + entryTypeName + " entries, grouped by package");
        return singleButtonPanel.getFirst();
    }

    @Override
    public void reload() {
        Map<Path, List<T>> packages = getPackageList();

        Map<String, List<T>> convertedEntries = new LinkedHashMap<>();

        for (Map.Entry<Path, List<T>> entry : packages.entrySet()) {
            Path path = entry.getKey();
            String keyAsString = path.toString();
            convertedEntries.put(keyAsString, entry.getValue());
        }

        populateEntries(convertedEntries);
    }

    void populateEntries(Map<String, List<T>> entriesByPackage) {
        DefaultMutableTreeNode rootNode = getRootNode();
        rootNode.removeAllChildren();
        loadAllEntries(entriesByPackage);
        sortAndExpandTree();
        repaint();
    }

    protected abstract Map<String, T> getRepository();

    protected abstract Map<Path, List<T>> getPackageList();

    protected abstract void setLoadedStatus();

    protected void loadAllEntries(Map<String, List<T>> entries) {
        Map<String, T> entriesRepository = getRepository();
        for (Map.Entry<String, List<T>> entry : entries.entrySet()) {
            Settings settings = SettingsManager.getSettings();
            String folderName = FileUtilities.extractFolderName(entry.getKey());
            GameDataPackage dataPackage = settings.getPackage(folderName);
            if (dataPackage == null || dataPackage.isDisabled()) {
                continue;
            }

            DefaultMutableTreeNode packageRoot = createPackageNode(entry, entriesRepository);
            DefaultMutableTreeNode rootNode = getRootNode();
            rootNode.add(packageRoot);
        }
        log.info("Total {} {} entries registered.", entriesRepository.size(), getEntryTypeName());
        setLoadedStatus();
    }

    private DefaultMutableTreeNode createPackageNode(Map.Entry<String, List<T>> entryFolder,
                                                     Map<String, T> entriesRepository) {
        String packagePath = entryFolder.getKey();
        String folderName = FileUtilities.extractFolderName(packagePath);
        Settings settings = SettingsManager.getSettings();

        DefaultMutableTreeNode result;
        if (SettingsManager.isCoreFolder(folderName)) {
            GameDataPackage corePackage = SettingsManager.getCorePackage();
            result = new DefaultMutableTreeNode(corePackage);
            for (T entry : entryFolder.getValue()) {
                MutableTreeNode entryNode = new DefaultMutableTreeNode(entry);
                entriesRepository.putIfAbsent(entry.getID(), entry);
                result.add(entryNode);
            }
        } else {
            GameDataPackage dataPackage = settings.getPackage(folderName);
            result = new DefaultMutableTreeNode(dataPackage);

            for (T entry : entryFolder.getValue()) {
                MutableTreeNode entryNode = new DefaultMutableTreeNode(entry);
                entriesRepository.putIfAbsent(entry.getID(), entry);
                result.add(entryNode);
            }
        }

        return result;
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
