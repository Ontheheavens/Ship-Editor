package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WingDataLoaded;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.instrument.ship.ShipInstrument;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Variant;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 04.08.2023
 */
public class WingsTreePanel extends CSVDataTreePanel<WingCSVEntry>{

    public WingsTreePanel() {
        super("Wing entries");
    }

    @Override
    protected Action getLoadDataAction() {
        return FileUtilities.getLoadWingDataAction();
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
    protected void updateEntryPanel(WingCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();

        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 5, 0, 5);
        BufferedImage sprite = selected.getWingMemberSprite();
        if (sprite != null) {
            String tooltip = selected.getEntryName();
            JLabel spriteIcon = ComponentUtilities.createIconFromImage(sprite, tooltip, 32);
            JPanel iconPanel = new JPanel();
            iconPanel.add(spriteIcon);
            rightPanel.add(iconPanel, constraints);
        }

        JPanel variantWrapper = new JPanel();
        variantWrapper.setLayout(new BoxLayout(variantWrapper, BoxLayout.PAGE_AXIS));

        List<Variant> memberVariant = Collections.singletonList(selected.retrieveMemberVariant());
        JPanel variantPanel = DataTreePanel.createVariantsPanel(memberVariant,
                new Dimension(0, 2));
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

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (cachedSelectForMenu.getUserObject() instanceof WingCSVEntry checked) {
            menu.addSeparator();

            JMenuItem addToHullBuiltIns = new JMenuItem("Add to hull built-in wings");
            ViewerLayer activeLayer = StaticController.getActiveLayer();
            addToHullBuiltIns.addActionListener(e -> {
                if (activeLayer instanceof ShipLayer checkedLayer) {
                    ShipHull hull = checkedLayer.getHull();
                    if (hull == null) return;
                    var builtInWings = hull.getBuiltInWings();
                    if (builtInWings == null) return;
                    if (WingsTreePanel.isPushEntryToListSuccessful(builtInWings, checkedLayer, checked)) {
                        EventBus.publish(new ActiveLayerUpdated(activeLayer));
                    }
                }
            });
            if (!WingsTreePanel.isCurrentLayerDataEligible() || WingsTreePanel.isNotActiveInstrument()) {
                addToHullBuiltIns.setEnabled(false);
            }
            menu.add(addToHullBuiltIns);

            JMenuItem addToSkinAdded = new JMenuItem("Add to skin built-in wings");
            addToSkinAdded.addActionListener(e -> {
                if (activeLayer instanceof ShipLayer checkedLayer) {

                    ShipPainter shipPainter = checkedLayer.getPainter();
                    if (shipPainter == null || shipPainter.isUninitialized()) return;
                    var skin = shipPainter.getActiveSkin();
                    if (skin == null || skin.isBase()) return;

                    var skinAdded = skin.getBuiltInWings();
                    if (WingsTreePanel.isPushEntryToListSuccessful(skinAdded, checkedLayer, checked)) {
                        EventBus.publish(new ActiveLayerUpdated(activeLayer));
                    }
                }
            });
            if (DataTreePanel.isCurrentSkinNotEligible() || WingsTreePanel.isNotActiveInstrument()) {
                addToSkinAdded.setEnabled(false);
            }
            menu.add(addToSkinAdded);

        }
        return menu;
    }

    private static boolean isNotActiveInstrument() {
        return ShipInstrumentsPane.getCurrentMode() != ShipInstrument.BUILT_IN_WINGS;
    }

    private static boolean isPushEntryToListSuccessful(List<WingCSVEntry> list, ShipLayer layer,
                                                       WingCSVEntry entry) {
        EditDispatch.postWingAdded(list, layer, entry);
        return true;
    }

    private static boolean isCurrentLayerDataEligible() {
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        boolean isShipLayer = activeLayer instanceof ShipLayer;
        ShipLayer shipLayer;
        if (isShipLayer) {
            shipLayer = (ShipLayer) activeLayer;
        } else return false;
        ShipHull hull = shipLayer.getHull();
        return hull != null && hull.getBuiltInWings() != null;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if(entry instanceof WingCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Wing ID: " + checked.getWingID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    protected Class<?> getEntryClass() {
        return WingCSVEntry.class;
    }

    private static class WingsTreeCellRenderer extends DefaultTreeCellRenderer {

        @SuppressWarnings("ParameterHidesMemberVariable")
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            if (object instanceof WingCSVEntry checked && leaf) {
                setText(checked.getEntryName());
            }

            return this;
        }

    }

}
