package oth.shipeditor.components.datafiles;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WingDataLoaded;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ShipSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.Variant;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 04.08.2023
 */
public class WingsTreePanel extends CSVDataTreePanel<WingCSVEntry>{

    WingsTreePanel() {
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
    String getTooltipForEntry(Object entry) {
        if(entry instanceof WingCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Wing ID: " + checked.getWingID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    Class<?> getEntryClass() {
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
                setText(WingsTreeCellRenderer.getEntryDisplayedName(checked));
            }

            return this;
        }

        private static String getEntryDisplayedName(WingCSVEntry entry) {
            String displayedName = entry.getDisplayedName();
            if (displayedName != null) {
                return displayedName;
            }
            Map<String, String> rowData = entry.getRowData();
            String variantID = rowData.get(StringConstants.VARIANT);
            GameDataRepository gameData = SettingsManager.getGameData();
            Map<String, Variant> allVariants = gameData.getAllVariants();
            Variant variant = allVariants.get(variantID);

            String hullID = variant.getHullId();

            ShipSpecFile desiredSpec = null;

            Map<String, ShipCSVEntry> allShipEntries = gameData.getAllShipEntries();
            outer: for (ShipCSVEntry shipEntry : allShipEntries.values()) {
                String shipEntryHullID = shipEntry.getHullID();
                if (shipEntryHullID.equals(hullID)) {
                    desiredSpec = shipEntry.getHullSpecFile();
                    break;
                } else {
                    Map<String, SkinSpecFile> skins = shipEntry.getSkins();
                    if (skins == null || skins.isEmpty()) continue;
                    for (SkinSpecFile skinSpec : skins.values()) {
                        String skinHullId = skinSpec.getSkinHullId();
                        if (skinHullId != null && skinHullId.equals(hullID)) {
                            desiredSpec = skinSpec;
                            break outer;
                        }
                    }
                }
            }

            if (desiredSpec != null) {
                String result = desiredSpec.getHullName() + " Wing";
                entry.setDisplayedName(result);
                return result;
            }

            return entry.getWingID();
        }

    }

}
