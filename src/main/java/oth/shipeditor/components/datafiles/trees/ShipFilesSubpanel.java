package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.Variant;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 10.07.2023
 */
class ShipFilesSubpanel extends JPanel {

    private final JPanel rightPanel;

    private static String currentShipHullID;

    ShipFilesSubpanel(JPanel parentPanel) {
        this.rightPanel = parentPanel;
    }

    JPanel createShipFilesPanel(ShipCSVEntry selected, HullsTreePanel parent) {
        JPanel shipFilesPanel = this;
        shipFilesPanel.setLayout(new BoxLayout(shipFilesPanel, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();

        currentShipHullID = null;

        Map<String, SkinSpecFile> skins = selected.getSkins();
        if (skins != null) {
            Collection<SkinSpecFile> values = skins.values();
            SkinSpecFile[] skinSpecFileArray = values.toArray(new SkinSpecFile[0]);
            JComboBox<SkinSpecFile> skinChooser = new JComboBox<>(skinSpecFileArray);
            skinChooser.setSelectedItem(selected.getActiveSkinSpecFile());
            skinChooser.addActionListener(e -> {
                SkinSpecFile chosen = (SkinSpecFile) skinChooser.getSelectedItem();
                selected.setActiveSkinSpecFile(chosen);
                parent.updateEntryPanel(selected);
            });
            skinChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

            constraints.insets = new Insets(0, 0, 0, 0);
            rightPanel.add(skinChooser, constraints);
        } else {
            rightPanel.removeAll();
        }

        JPanel labelContainer = ShipFilesSubpanel.createLabelContainer(selected);
        shipFilesPanel.add(labelContainer);

        ShipFilesSubpanel.addHullmodPanel(shipFilesPanel, selected);

        JPanel variantsPanel = ShipFilesSubpanel.createVariantPanel();
        if (variantsPanel != null) {
            shipFilesPanel.add(variantsPanel);
        }

        return shipFilesPanel;
    }

    private static JPanel createLabelContainer(ShipCSVEntry selected) {
        Map<String, String> rowData = selected.getRowData();
        String shipName = rowData.get(StringConstants.NAME);
        String shipId = selected.getHullID();
        String hullFileName = selected.getHullFileName();

        HullSpecFile selectedHullFileSpecFile = selected.getHullSpecFile();
        SkinSpecFile activeSkinSpecFile = selected.getActiveSkinSpecFile();
        Path skinFilePath = null;

        String spriteFileName = selectedHullFileSpecFile.getSpriteName();
        String skinFileName = "";

        if (activeSkinSpecFile != null && !activeSkinSpecFile.isBase()) {
            shipName = activeSkinSpecFile.getHullName();
            shipId = activeSkinSpecFile.getSkinHullId();
            spriteFileName = activeSkinSpecFile.getSpriteName();
            skinFilePath = activeSkinSpecFile.getFilePath();
            skinFileName = skinFilePath.getFileName().toString();
        }

        if (spriteFileName == null || spriteFileName.isEmpty()) {
            spriteFileName = selectedHullFileSpecFile.getSpriteName();
        }

        Path shipFilePath = selectedHullFileSpecFile.getFilePath();
        String shipFilePathName = shipFilePath.toString();

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(0, 4, 0, 0));

        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));
        JLabel shipNameLabel = new JLabel("Ship name: " + shipName);
        shipNameLabel.setBorder(new EmptyBorder(ComponentUtilities.createLabelInsets()));
        labelContainer.add(shipNameLabel);

        labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

        currentShipHullID = shipId;

        JLabel shipIDLabel = new JLabel("Ship ID: " + shipId);
        shipIDLabel.setBorder(new EmptyBorder(ComponentUtilities.createLabelInsets()));
        labelContainer.add(shipIDLabel);

        labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

        JLabel hullFileNameLabel = new JLabel("Hull file : " + hullFileName);
        hullFileNameLabel.setToolTipText(shipFilePathName);
        hullFileNameLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        JPopupMenu hullContextMenu = ComponentUtilities.createPathContextMenu(shipFilePath);
        hullFileNameLabel.addMouseListener(new MouseoverLabelListener(hullContextMenu, hullFileNameLabel));
        labelContainer.add(hullFileNameLabel);

        labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

        File spriteFile = FileLoading.fetchDataFile(Path.of(spriteFileName),
                selected.getPackageFolderPath());
        JLabel spriteFileNameLabel;
        if (spriteFile != null) {
            spriteFileNameLabel = new JLabel("Sprite file: : " + spriteFile.getName());
        } else {
            spriteFileNameLabel = new JLabel("Sprite file: failed to fetch! ");
        }
        spriteFileNameLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        JPopupMenu spriteContextMenu;
        if (spriteFile != null) {
            spriteContextMenu = ComponentUtilities.createPathContextMenu(spriteFile.toPath());
            spriteFileNameLabel.addMouseListener(new MouseoverLabelListener(spriteContextMenu, spriteFileNameLabel));
            spriteFileNameLabel.setToolTipText(spriteFile.toString());
        }
        labelContainer.add(spriteFileNameLabel);

        if (!skinFileName.isEmpty()) {
            labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

            JLabel skinFileNameLabel = new JLabel("Skin file: " + skinFileName);
            skinFileNameLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
            JPopupMenu skinContextMenu = ComponentUtilities.createPathContextMenu(skinFilePath);
            skinFileNameLabel.addMouseListener(new MouseoverLabelListener(skinContextMenu, skinFileNameLabel));
            skinFileNameLabel.setToolTipText(skinFilePath.toString());
            labelContainer.add(skinFileNameLabel);
        }

        labelContainer.add(Box.createRigidArea(new Dimension(0, 4)));

        return labelContainer;
    }

    private static Dimension createPadding() {
        return new Dimension(0,2);
    }

    private static void addHullmodPanel(JPanel panel, ShipCSVEntry selected) {
        GameDataRepository gameData = SettingsManager.getGameData();
        if (!gameData.isHullmodDataLoaded()) return;

        MatteBorder matteLine = new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY);
        Border titledBorder = new TitledBorder(matteLine, "Built-in hullmods",
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
        JPanel hullmodsPanel = new JPanel();
        hullmodsPanel.setBorder(titledBorder);
        hullmodsPanel.setAlignmentX(LEFT_ALIGNMENT);

        Collection<String> hullmodIDs = selected.getBuiltInHullmods();

        if (hullmodIDs.isEmpty()) return;

        Map<String, HullmodCSVEntry> allHullmods = gameData.getAllHullmodEntries();
        for (String id : hullmodIDs) {
            HullmodCSVEntry entry = allHullmods.get(id);
            JLabel imageLabel = ComponentUtilities.createHullmodIcon(entry);
            hullmodsPanel.add(imageLabel);
        }

        panel.add(hullmodsPanel);
        panel.revalidate();
        panel.repaint();
    }

    private static JPanel createVariantPanel() {
        GameDataRepository gameData = SettingsManager.getGameData();

        MatteBorder matteLine = new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY);
        Border titledBorder = new TitledBorder(matteLine, "Variants",
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
        JPanel variantsPanel = new JPanel();
        variantsPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        variantsPanel.setBorder(titledBorder);
        variantsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(2, 0, 0, 0));
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));

        Collection<Variant> variantsForHull = new ArrayList<>();
        Map<String, Variant> allVariants = gameData.getAllVariants();
        for (Variant variant : allVariants.values()) {
            String hullID = variant.getHullId();
            if (hullID.equals(currentShipHullID)) {
                variantsForHull.add(variant);
            }
        }

        if (variantsForHull.isEmpty()) return null;
        variantsForHull.forEach(variant -> {
            Path variantFilePath = variant.getVariantFilePath();
            JLabel variantLabel = new JLabel("Variant file : " + variantFilePath.getFileName());
            variantLabel.setToolTipText(String.valueOf(variantFilePath));
            variantLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
            JPopupMenu pathContextMenu = ComponentUtilities.createPathContextMenu(variantFilePath);
            variantLabel.addMouseListener(new MouseoverLabelListener(pathContextMenu, variantLabel));
            labelContainer.add(variantLabel);
            labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));
        });

        variantsPanel.add(labelContainer);
        return variantsPanel;
    }

}
