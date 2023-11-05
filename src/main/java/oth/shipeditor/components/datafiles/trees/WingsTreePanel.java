package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WingDataLoaded;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.GameDataPackage;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 04.08.2023
 */
public class WingsTreePanel extends CSVDataTreePanel<WingCSVEntry>{

    public WingsTreePanel() {
        super("Wing entry packages");
    }

    @Override
    protected Action getLoadDataAction() {
        return FileLoading.loadDataAsync(FileLoading.getLoadWings());
    }

    @Override
    protected JTree createCustomTree() {
        JTree custom = super.createCustomTree();
        custom.setCellRenderer(new WingsTreeCellRenderer());
        return custom;
    }

    @Override
    protected String getEntryTypeName() {
        return "wing";
    }

    @Override
    protected Map<String, WingCSVEntry> getRepository() {
        GameDataRepository gameData = SettingsManager.getGameData();
        return gameData.getAllWingEntries();
    }

    @Override
    protected Map<Path, List<WingCSVEntry>> getPackageList() {
        GameDataRepository gameData = SettingsManager.getGameData();
        return gameData.getWingEntriesByPackage();
    }

    @Override
    protected void setLoadedStatus() {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setWingDataLoaded(true);
    }

    @Override
    protected void initWalkerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof WingDataLoaded checked) {
                Map<String, List<WingCSVEntry>> wingsByPackage = checked.wingsByPackage();
                if (wingsByPackage == null) {
                    throw new RuntimeException("Wing data initialization failed: table data is NULL!");
                }
                populateEntries(wingsByPackage);
            }
        });
    }

    @Override
    void populateEntries(Map<String, List<WingCSVEntry>> entriesByPackage) {
        GameDataRepository data = SettingsManager.getGameData();
        if (!data.isShipDataLoaded()) return;
        super.populateEntries(entriesByPackage);
    }

    @Override
    protected void updateEntryPanel(WingCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();

        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 5, 0, 5);
        JLabel spriteIcon = selected.getIconLabel(128);
        if (spriteIcon != null) {
            JPanel iconPanel = new JPanel();
            iconPanel.add(spriteIcon);
            rightPanel.add(iconPanel, constraints);
        }

        JPanel variantWrapper = new JPanel();
        variantWrapper.setLayout(new BoxLayout(variantWrapper, BoxLayout.PAGE_AXIS));

        List<VariantFile> memberVariantFile = Collections.singletonList(selected.retrieveMemberVariant());
        JPanel variantPanel = DataTreePanel.createVariantsPanel(memberVariantFile, false);
        variantWrapper.add(variantPanel);

        constraints.gridy = 1;
        rightPanel.add(variantWrapper, constraints);

        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    @Override
    protected WingCSVEntry getObjectFromNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (!(userObject instanceof WingCSVEntry checked)) return null;
        return checked;
    }

    @Override
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (cachedSelectForMenu.getUserObject() instanceof WingCSVEntry checked) {
            switch (StaticController.getEditorMode()) {
                case BUILT_IN_WINGS -> {
                    menu.addSeparator();
                    WingsTreePanel.populateBuiltInOptions(menu, checked);
                }
                case VARIANT_DATA -> {
                    menu.addSeparator();
                    WingsTreePanel.populateVariantOptions(menu, checked);
                }
                default -> {}
            }
        }
        return menu;
    }

    private static void populateVariantOptions(JPopupMenu menu, WingCSVEntry entry) {
        EditorInstrument targetMode = EditorInstrument.VARIANT_DATA;

        JMenuItem addToVariantWings = new JMenuItem("Add to variant wings");
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        addToVariantWings.addActionListener(e -> {
            if (activeLayer instanceof ShipLayer checkedLayer) {
                var variant = checkedLayer.getActiveVariant();
                if (variant == null) return;
                var variantWings = variant.getWings();
                if (WingsTreePanel.isPushEntryToListSuccessful(variantWings, checkedLayer, entry)) {
                    EventBus.publish(new ActiveLayerUpdated(activeLayer));
                }
            }
        });
        if (!WingsTreePanel.isCurrentLayerVariantEligible() || WingsTreePanel.isNotActiveInstrument(targetMode)) {
            addToVariantWings.setEnabled(false);
        }
        menu.add(addToVariantWings);
    }

    private static void populateBuiltInOptions(JPopupMenu menu, WingCSVEntry entry) {
        EditorInstrument targetMode = EditorInstrument.BUILT_IN_WINGS;

        JMenuItem addToHullBuiltIns = new JMenuItem("Add to hull built-in wings");
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        addToHullBuiltIns.addActionListener(e -> {
            if (activeLayer instanceof ShipLayer checkedLayer) {
                ShipHull hull = checkedLayer.getHull();
                if (hull == null) return;
                var builtInWings = hull.getBuiltInWings();
                if (builtInWings == null) return;
                if (WingsTreePanel.isPushEntryToListSuccessful(builtInWings, checkedLayer, entry)) {
                    EventBus.publish(new ActiveLayerUpdated(activeLayer));
                }
            }
        });
        if (!WingsTreePanel.areCurrentLayerBuiltInsEligible() || WingsTreePanel.isNotActiveInstrument(targetMode)) {
            addToHullBuiltIns.setEnabled(false);
        }
        menu.add(addToHullBuiltIns);

        JMenuItem addToSkinAdded = WingsTreePanel.getAddToSkinAdded(entry, activeLayer);
        menu.add(addToSkinAdded);
    }

    private static JMenuItem getAddToSkinAdded(WingCSVEntry checked, ViewerLayer activeLayer) {
        EditorInstrument targetMode = EditorInstrument.BUILT_IN_WINGS;
        JMenuItem addToSkinAdded = new JMenuItem("Add to skin built-in wings");
        addToSkinAdded.addActionListener(e -> {
            if (activeLayer instanceof ShipLayer checkedLayer) {
                var skin = checkedLayer.getActiveSkin();
                if (skin == null) return;
                var skinAdded = skin.getBuiltInWings();
                if (WingsTreePanel.isPushEntryToListSuccessful(skinAdded, checkedLayer, checked)) {
                    EventBus.publish(new ActiveLayerUpdated(activeLayer));
                }
            }
        });
        if (DataTreePanel.isCurrentSkinNotEligible() || WingsTreePanel.isNotActiveInstrument(targetMode)) {
            addToSkinAdded.setEnabled(false);
        }
        return addToSkinAdded;
    }

    private static boolean isNotActiveInstrument(EditorInstrument target) {
        return StaticController.getEditorMode() != target;
    }

    private static boolean isPushEntryToListSuccessful(List<WingCSVEntry> list, ShipLayer layer,
                                                       WingCSVEntry entry) {
        EditDispatch.postWingAdded(list, layer, entry);
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean areCurrentLayerBuiltInsEligible() {
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        boolean isShipLayer = activeLayer instanceof ShipLayer;
        ShipLayer shipLayer;
        if (isShipLayer) {
            shipLayer = (ShipLayer) activeLayer;
        } else return false;
        ShipHull hull = shipLayer.getHull();
        return hull != null && hull.getBuiltInWings() != null;
    }

    private static boolean isCurrentLayerVariantEligible() {
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        boolean isShipLayer = activeLayer instanceof ShipLayer;
        ShipLayer shipLayer;
        if (isShipLayer) {
            shipLayer = (ShipLayer) activeLayer;
        } else return false;
        var variant = shipLayer.getActiveVariant();
        if (variant != null) {
            var wings = variant.getWings();
            return wings != null;
        } else return false;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if (entry instanceof WingCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Wing ID: " + checked.getWingID() + "</p>" +
                    "</html>";
        }
        return super.getTooltipForEntry(entry);
    }

    @Override
    protected Class<?> getEntryClass() {
        return WingCSVEntry.class;
    }

    private static class WingsTreeCellRenderer extends DefaultTreeCellRenderer {

        @SuppressWarnings({"ParameterHidesMemberVariable", "ChainOfInstanceofChecks"})
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            setForeground(Color.BLACK);
            if (object instanceof WingCSVEntry checked && leaf) {
                setText(checked.getEntryName());
            } else if (object instanceof GameDataPackage dataPackage) {
                setText(dataPackage.getFolderName());
                if (SettingsManager.isCoreFolder(dataPackage)) {
                    setForeground(Color.RED);
                } else if (dataPackage.isPinned()) {
                    setForeground(Color.BLUE);
                }
            }

            return this;
        }

    }

}
