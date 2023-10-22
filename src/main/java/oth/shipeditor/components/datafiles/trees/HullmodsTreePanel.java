package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullmodFoldersWalked;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.OrdnancedCSVEntry;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        return FileLoading.getLoadHullmodDataAction();
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

    private static JPanel createHullmodIconPanel(OrdnancedCSVEntry selected) {
        JLabel imageLabel = selected.getIconLabel();
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

    @Override
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (cachedSelectForMenu.getUserObject() instanceof HullmodCSVEntry checked) {
            switch (StaticController.getEditorMode()) {
                case BUILT_IN_MODS -> {
                    menu.addSeparator();
                    populateBuiltInOptions(menu, checked);
                }
                case VARIANT_DATA -> {
                    menu.addSeparator();
                    populateVariantOptions(menu, checked);
                }
                default -> {}
            }
        }
        return menu;
    }

    private void populateVariantOptions(JPopupMenu menu, HullmodCSVEntry entry) {
        ViewerLayer activeLayer = StaticController.getActiveLayer();

        JMenuItem addToNormal = createVariantOption("Add to variant mods",
                activeLayer, entry, ShipVariant::getHullMods);
        menu.add(addToNormal);

        JMenuItem addToPermanent = createVariantOption("Add to variant perma-mods",
                activeLayer, entry, ShipVariant::getPermaMods);
        menu.add(addToPermanent);

        JMenuItem addToSMods = createVariantOption("Add to variant S-mods",
                activeLayer, entry, ShipVariant::getSMods);
        menu.add(addToSMods);
    }

    private JMenuItem createVariantOption(String text, ViewerLayer activeLayer,
                                          HullmodCSVEntry entry, Function<ShipVariant, List<HullmodCSVEntry>> getter) {
        EditorInstrument targetMode = EditorInstrument.VARIANT_DATA;

        JMenuItem option = new JMenuItem(text);
        option.addActionListener(e -> {
            var modsList = HullmodsTreePanel.getVariantMods(activeLayer, getter);
            if (modsList == null) return;
            commenceModAddition(modsList, (ShipLayer) activeLayer, entry);
        });
        boolean isListEligible = HullmodsTreePanel.getVariantMods(activeLayer, getter) != null;
        if (!isListEligible || HullmodsTreePanel.isNotActiveInstrument(targetMode)) {
            option.setEnabled(false);
        }
        return option;
    }

    private static List<HullmodCSVEntry> getVariantMods(ViewerLayer activeLayer,
                                                        Function<ShipVariant, List<HullmodCSVEntry>> getter) {
        if (activeLayer instanceof ShipLayer checkedLayer) {
            var activeVariant = checkedLayer.getActiveVariant();
            if (activeVariant == null) return null;
            return getter.apply(activeVariant);
        } else return null;
    }

    @SuppressWarnings({"ExtractMethodRecommender", "OverlyComplexMethod"})
    private void populateBuiltInOptions(JPopupMenu menu, HullmodCSVEntry entry) {
        EditorInstrument targetMode = EditorInstrument.BUILT_IN_MODS;
        JMenuItem addToHullBuiltIns = new JMenuItem("Add to hull built-ins");
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        addToHullBuiltIns.addActionListener(e -> {
            if (activeLayer instanceof ShipLayer checkedLayer) {
                ShipHull hull = checkedLayer.getHull();
                if (hull == null) return;
                var builtInMods = hull.getBuiltInMods();
                if (builtInMods == null) return;
                commenceModAddition(builtInMods, checkedLayer, entry);
            }
        });
        if (!HullmodsTreePanel.isCurrentLayerDataEligible() || HullmodsTreePanel.isNotActiveInstrument(targetMode)) {
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
                commenceModAddition(skinAdded, checkedLayer, entry);
            }
        });
        if (DataTreePanel.isCurrentSkinNotEligible() || HullmodsTreePanel.isNotActiveInstrument(targetMode)) {
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
                commenceModAddition(skinRemoved, checkedLayer, entry);
            }
        });
        if (DataTreePanel.isCurrentSkinNotEligible() || HullmodsTreePanel.isNotActiveInstrument(targetMode)) {
            addToSkinRemoved.setEnabled(false);
        }
        menu.add(addToSkinRemoved);
    }

    private void commenceModAddition(List<HullmodCSVEntry> list, ShipLayer shipLayer, HullmodCSVEntry entry) {
        if (this.isPushEntryToListSuccessful(list, shipLayer, entry)) {
            EventBus.publish(new ActiveLayerUpdated(shipLayer));
        }
    }

    private static boolean isNotActiveInstrument(EditorInstrument target) {
        return StaticController.getEditorMode() != target;
    }

    private boolean isPushEntryToListSuccessful(List<HullmodCSVEntry> list, ShipLayer layer,
                                                HullmodCSVEntry entry) {
        if (list.contains(entry)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot add hullmod entry: already present in list!");
            return false;
        } else {
            EditDispatch.postHullmodAdded(list, layer, entry);
            return true;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
