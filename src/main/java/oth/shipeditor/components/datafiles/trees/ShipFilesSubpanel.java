package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.ship.SkinSpecFile;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

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

        ShipFilesSubpanel.addSpritePreview(shipFilesPanel, selected);

        JPanel labelContainer = ShipFilesSubpanel.createLabelContainer(selected);
        shipFilesPanel.add(labelContainer);

        ShipFilesSubpanel.addHullmodPanel(shipFilesPanel, selected);

        JPanel variantsPanel = ShipFilesSubpanel.createVariantPanel();
        if (variantsPanel != null) {
            shipFilesPanel.add(variantsPanel);
        }

        return shipFilesPanel;
    }

    private static void addSpritePreview(JPanel shipFilesPanel, ShipCSVEntry selected) {
        String spriteFileName = selected.getShipSpriteName();
        File spriteFile = FileLoading.fetchDataFile(Path.of(spriteFileName), selected.getPackageFolderPath());
        if (spriteFile != null) {
            Sprite sprite = FileLoading.loadSprite(spriteFile);
            String tooltip = Utility.getTooltipForSprite(sprite);
            JLabel spriteIcon = ComponentUtilities.createIconFromImage(sprite.getImage(), tooltip, 128);
            spriteIcon.setAlignmentX(0.5f);

            JPanel spriteWrapper = new JPanel();
            spriteWrapper.setAlignmentX(LEFT_ALIGNMENT);

            MatteBorder matteLine = new MatteBorder(new Insets(0, 0, 1, 0),
                    Color.LIGHT_GRAY);
            Border titledBorder = new TitledBorder(matteLine, StringValues.FILES,
                    TitledBorder.CENTER, TitledBorder.BOTTOM);
            spriteWrapper.setBorder(titledBorder);

            spriteWrapper.add(spriteIcon);

            shipFilesPanel.add(spriteWrapper);
        }
    }

    private static JPanel createLabelContainer(ShipCSVEntry selected) {
        String shipName = selected.getShipName();
        String shipId = selected.getShipID();
        String hullFileName = selected.getHullFileName();

        HullSpecFile selectedHullFileSpecFile = selected.getHullSpecFile();
        SkinSpecFile activeSkinSpecFile = selected.getActiveSkinSpecFile();
        Path skinFilePath = null;

        String spriteFileName = selected.getShipSpriteName();
        String skinFileName = "";

        if (activeSkinSpecFile != null && !activeSkinSpecFile.isBase()) {
            skinFilePath = activeSkinSpecFile.getFilePath();
            skinFileName = skinFilePath.getFileName().toString();
        }

        Path shipFilePath = selectedHullFileSpecFile.getFilePath();
        String shipFilePathName = shipFilePath.toString();

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(0, 4, 0, 0));

        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));
        JLabel shipNameLabel = new JLabel("Ship filename: " + shipName);
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

        JPanel hullmodsPanel = new JPanel();
        ComponentUtilities.outfitPanelWithTitle(hullmodsPanel,
                new Insets(1, 0, 0, 0), "Built-in hullmods");
        hullmodsPanel.setAlignmentX(LEFT_ALIGNMENT);

        Collection<String> hullmodIDs = selected.getBuiltInHullmods();

        if (hullmodIDs.isEmpty()) return;

        Map<String, HullmodCSVEntry> allHullmods = gameData.getAllHullmodEntries();
        for (String id : hullmodIDs) {
            HullmodCSVEntry entry = allHullmods.get(id);
            if (entry != null) {
                JLabel imageLabel = entry.getIconLabel();
                hullmodsPanel.add(imageLabel);
            }
        }

        panel.add(hullmodsPanel);
        panel.revalidate();
        panel.repaint();
    }

    private static JPanel createVariantPanel() {
        GameDataRepository gameData = SettingsManager.getGameData();

        Collection<VariantFile> variantsForHull = new ArrayList<>();
        Map<String, VariantFile> allVariants = gameData.getAllVariants();
        for (VariantFile variantFile : allVariants.values()) {
            String hullID = variantFile.getHullId();
            if (hullID.equals(currentShipHullID)) {
                variantsForHull.add(variantFile);
            }
        }

        if (variantsForHull.isEmpty()) return null;
        return DataTreePanel.createVariantsPanel(variantsForHull, true);
    }

}
