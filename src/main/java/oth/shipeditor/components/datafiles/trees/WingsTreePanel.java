package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WingDataLoaded;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Variant;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

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
