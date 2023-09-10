package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodFoldersWalked;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@Log4j2
public
class HullmodsTreePanel extends CSVDataTreePanel<HullmodCSVEntry>{

    public HullmodsTreePanel() {
        super("Hullmod files");
    }

    @Override
    protected String getEntryTypeName() {
        return "hullmod";
    }

    @Override
    protected Action getLoadDataAction() {
        return FileUtilities.getLoadHullmodDataAction();
    }

    @Override
    protected Map<String, HullmodCSVEntry> getRepository() {
        GameDataRepository gameData = SettingsManager.getGameData();
        return gameData.getAllHullmodEntries();
    }

    @Override
    protected void setLoadedStatus() {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setHullmodDataLoaded(true);
    }

    @Override
    protected HullmodCSVEntry getObjectFromNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (!(userObject instanceof HullmodCSVEntry checked)) return null;
        return checked;
    }

    @Override
    protected void initWalkerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof HullmodFoldersWalked checked) {
                Map<String, List<HullmodCSVEntry>> hullmods = checked.hullmodsByPackage();
                if (hullmods == null) {
                    throw new RuntimeException("Hullmod data initialization failed: table data is NULL!");
                }
                populateEntries(hullmods);
            }
        });
    }

    protected void updateEntryPanel(HullmodCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 0, 5);
        String spriteFileName = selected.getSpriteFileName();
        if (spriteFileName != null && !spriteFileName.isEmpty()) {
            JPanel hullmodIconPanel = HullmodsTreePanel.createHullmodIconPanel(selected);
            rightPanel.add(hullmodIconPanel, constraints);
        }
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    private static JPanel createHullmodIconPanel(HullmodCSVEntry selected) {
        JLabel imageLabel = ComponentUtilities.createHullmodIcon(selected);
        JPanel iconPanel = new JPanel();
        iconPanel.add(imageLabel);
        return iconPanel;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if(entry instanceof HullmodCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Hullmod ID: " + checked.getHullmodID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (cachedSelectForMenu.getUserObject() instanceof HullmodCSVEntry checked) {
            menu.addSeparator();

            JMenuItem addToHullBuiltIns = new JMenuItem("Add to hull built-ins");
            ViewerLayer activeLayer = StaticController.getActiveLayer();
            addToHullBuiltIns.addActionListener(e -> {
                if (activeLayer instanceof ShipLayer checkedLayer) {
                    ShipHull hull = checkedLayer.getHull();
                    if (hull == null) return;
                    var builtInMods = hull.getBuiltInMods();
                    if (builtInMods == null) return;
                    if (this.isPushEntryToListSuccessful(builtInMods, checkedLayer, checked)) {
                        EventBus.publish(new ActiveLayerUpdated(activeLayer));
                    }
                }
            });
            if (!HullmodsTreePanel.isCurrentLayerDataEligible() || HullmodsTreePanel.isNotActiveInstrument()) {
                addToHullBuiltIns.setEnabled(false);
            }
            menu.add(addToHullBuiltIns);

            JMenuItem addToSkinAdded = new JMenuItem("Add to skin built-ins");
            addToSkinAdded.addActionListener(e -> {
                if (activeLayer instanceof ShipLayer checkedLayer) {

                    ShipPainter shipPainter = checkedLayer.getPainter();
                    if (shipPainter == null || shipPainter.isUninitialized()) return;
                    var skin = shipPainter.getActiveSkin();
                    if (skin == null || skin.isBase()) return;

                    var skinAdded = skin.getBuiltInMods();
                    if (this.isPushEntryToListSuccessful(skinAdded, checkedLayer, checked)) {
                        EventBus.publish(new ActiveLayerUpdated(activeLayer));
                    }
                }
            });
            if (DataTreePanel.isCurrentSkinNotEligible() || HullmodsTreePanel.isNotActiveInstrument()) {
                addToSkinAdded.setEnabled(false);
            }
            menu.add(addToSkinAdded);

            JMenuItem addToSkinRemoved = new JMenuItem("Add to skin built-in removals");
            addToSkinRemoved.addActionListener(e -> {
                if (activeLayer instanceof ShipLayer checkedLayer) {

                    ShipPainter shipPainter = checkedLayer.getPainter();
                    if (shipPainter == null || shipPainter.isUninitialized()) return;
                    var skin = shipPainter.getActiveSkin();
                    if (skin == null || skin.isBase()) return;

                    var skinRemoved = skin.getRemoveBuiltInMods();
                    if (this.isPushEntryToListSuccessful(skinRemoved, checkedLayer, checked)) {
                        EventBus.publish(new ActiveLayerUpdated(activeLayer));
                    }
                }
            });
            if (DataTreePanel.isCurrentSkinNotEligible() || HullmodsTreePanel.isNotActiveInstrument()) {
                addToSkinRemoved.setEnabled(false);
            }
            menu.add(addToSkinRemoved);

        }
        return menu;
    }

    private static boolean isNotActiveInstrument() {
        return StaticController.getEditorMode() != EditorInstrument.BUILT_IN_MODS;
    }

    private boolean isPushEntryToListSuccessful(List<HullmodCSVEntry> list, ShipLayer layer,
                                                HullmodCSVEntry entry) {
        if (list.contains(entry)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot add hullmod entry: already present in list!");
            return false;
        } else {
            EditDispatch.postHullmodAdded(list, layer, entry);
        }
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
        return hull != null && hull.getBuiltInMods() != null;
    }

    @Override
    protected Class<?> getEntryClass() {
        return HullmodCSVEntry.class;
    }

}
