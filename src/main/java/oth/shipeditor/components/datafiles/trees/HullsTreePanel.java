package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.GameDataPanelResized;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.communication.events.files.HullTreeEntryCleared;
import oth.shipeditor.communication.events.files.HullTreeReloadQueued;
import oth.shipeditor.components.datafiles.OpenDataTarget;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.HullSize;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
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
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
public
class HullsTreePanel extends DataTreePanel {

    private ShipCSVEntry cachedEntry;

    private boolean filtersOpened;

    public HullsTreePanel() {
        super("Hull files");
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if(entry instanceof ShipCSVEntry checked) {
            HullSpecFile hullSpecFileFile = checked.getHullSpecFile();
            return "<html>" +
                    "<p>" + "Hull size: " + hullSpecFileFile.getHullSize() + "</p>" +
                    "<p>" + "Hull ID: " + checked.getHullID() + "</p>" +
                    "<p>" + "(Double-click to load as layer)" + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    protected void initTreePanelListeners(JPanel passedTreePanel) {
        this.initBusListening();
        this.initComponentListeners();
    }

    private void initBusListening() {
        JTree tree = getTree();
        EventBus.subscribe(event -> {
            if (event instanceof HullTreeEntryCleared) {
                cachedEntry = null;
                resetInfoPanel();
                repaint();
                tree.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof HullTreeReloadQueued) {
                this.reload();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SelectShipDataEntry checked) {
                ShipCSVEntry entry = checked.entry();
                DefaultMutableTreeNode node = getNodeOfEntry(entry);
                if (node != null) {
                    TreePath path = new TreePath(node.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                }
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    public void reload() {
        JTree tree = getTree();
        DefaultMutableTreeNode rootNode = getRootNode();
        rootNode.removeAllChildren();
        reloadHullList();
        sortAndExpandTree();
        repaint();
        tree.repaint();
    }

    protected JPanel createSearchContainer() {
        JPanel searchContainer = new JPanel(new GridBagLayout());
        searchContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
        JTextField searchField = HullsTreePanel.getSearchField();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        searchContainer.add(searchField, gridBagConstraints);
        JButton searchButton = new JButton(StringValues.SEARCH);
        searchButton.addActionListener(e -> reload());
        searchContainer.add(searchButton);
        return searchContainer;
    }

    private static JTextField getSearchField() {
        JTextField searchField = new JTextField();
        searchField.setToolTipText("Input is checked against displayed filename and base hull ID as a substring.");
        Document document = searchField.getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ShipFilterPanel.setCurrentTextFilter(searchField.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                ShipFilterPanel.setCurrentTextFilter(searchField.getText());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                ShipFilterPanel.setCurrentTextFilter(searchField.getText());
            }
        });
        return searchField;
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
                filtersOpened = false;
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

        cachedEntry = selected;

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void reloadHullList() {
        Map<Path, List<ShipCSVEntry>> shipEntries = ShipFilterPanel.getFilteredEntries();

        if (shipEntries == null || shipEntries.isEmpty()) return;

        for (Map.Entry<Path, List<ShipCSVEntry>> hullFolder : shipEntries.entrySet()) {
            String packageName = hullFolder.getKey().getFileName().toString();
            DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(packageName);

            for (ShipCSVEntry entry : hullFolder.getValue()) {
                MutableTreeNode shipNode = new DefaultMutableTreeNode(entry);
                packageRoot.add(shipNode);
            }
            DefaultMutableTreeNode rootNode = getRootNode();
            rootNode.add(packageRoot);
        }
    }

    @Override
    protected JPanel createTopPanel() {
        Pair<JPanel, JButton> singleButtonPanel = ComponentUtilities.createSingleButtonPanel("",
                FileLoading.getLoadShipDataAction());
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload ship data");
        button.setToolTipText("Reload all ship, skin and variant files, grouped by package");

        JPanel buttonPanel = singleButtonPanel.getFirst();

        JButton filtersButton = new JButton(StringValues.FILTERS);
        filtersButton.addActionListener(e -> {
            if (filtersOpened) {
                if (cachedEntry != null) {
                    updateEntryPanel(cachedEntry);
                } else {
                    resetInfoPanel();
                }
                filtersOpened = false;
            } else {
                JPanel rightPanel = getRightPanel();
                rightPanel.removeAll();

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.insets = new Insets(0, 0, 0, 0);

                JPanel panel = new ShipFilterPanel();
                rightPanel.add(panel, constraints);

                filtersOpened = true;
                rightPanel.revalidate();
                rightPanel.repaint();
            }
        });

        buttonPanel.add(filtersButton);

        return buttonPanel;
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
    protected Class<?> getEntryClass() {
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
        JMenuItem loadAsLayer = new JMenuItem("Load as ship layer");
        loadAsLayer.addActionListener(new HullsTreePanel.LoadLayerFromTree());
        menu.add(loadAsLayer);
        return menu;
    }

    private static JMenuItem addOpenSkinOption(ShipCSVEntry checked) {
        SkinSpecFile activeSkinSpecFile = checked.getActiveSkinSpecFile();
        if (activeSkinSpecFile == null || activeSkinSpecFile.isBase()) return null;
        JMenuItem openSkin = new JMenuItem("Open skin file");
        openSkin.addActionListener(e -> {
            Path toOpen = activeSkinSpecFile.getFilePath();
            FileUtilities.openPathInDesktop(toOpen);
        });
        return openSkin;
    }

    @Override
    protected void openEntryPath(OpenDataTarget target) {
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (!(cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry checked)) return;
        HullSpecFile hullSpecFileFile = checked.getHullSpecFile();
        Path toOpen;
        switch (target) {
            case FILE -> toOpen = hullSpecFileFile.getFilePath();
            case CONTAINER -> toOpen = hullSpecFileFile.getFilePath().getParent();
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
                HullSize hullSize = checked.getSize();
                switch (hullSize) {
                    case FIGHTER -> setIcon(HullSize.FIGHTER.getIcon());
                    case FRIGATE -> setIcon(HullSize.FRIGATE.getIcon());
                    case DESTROYER -> setIcon(HullSize.DESTROYER.getIcon());
                    case CRUISER -> setIcon(HullSize.CRUISER.getIcon());
                    case CAPITAL_SHIP -> setIcon(HullSize.CAPITAL_SHIP.getIcon());
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
