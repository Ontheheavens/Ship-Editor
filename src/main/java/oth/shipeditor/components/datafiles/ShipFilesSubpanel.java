package oth.shipeditor.components.datafiles;

import com.formdev.flatlaf.ui.FlatLineBorder;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.MouseoverLabelListener;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 10.07.2023
 */
class ShipFilesSubpanel extends JPanel {

    private final JPanel rightPanelReference;


    ShipFilesSubpanel(JPanel rightPanel) {
        this.rightPanelReference = rightPanel;
    }


    JPanel createShipFilesPanel(ShipCSVEntry selected, HullsTreePanel parent) {
        JPanel shipFilesPanel = this;
        shipFilesPanel.setLayout(new BoxLayout(shipFilesPanel, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();

        Map<String, Skin> skins = selected.getSkins();
        if (skins != null) {
            Collection<Skin> values = skins.values();
            Skin[] skinArray = values.toArray(new Skin[0]);
            JComboBox<Skin> skinChooser = new JComboBox<>(skinArray);
            skinChooser.setSelectedItem(selected.getActiveSkin());
            skinChooser.addActionListener(e -> {
                Skin chosen = (Skin) skinChooser.getSelectedItem();
                selected.setActiveSkin(chosen);
                parent.updateEntryPanel(selected);
            });
            skinChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

            constraints.insets = new Insets(0, 0, 0, 0);
            rightPanelReference.add(skinChooser, constraints);
        } else {
            rightPanelReference.removeAll();
        }

        JPanel labelContainer = ShipFilesSubpanel.createLabelContainer(selected);

        shipFilesPanel.add(labelContainer);

        ShipFilesSubpanel.addHullmodPanel(shipFilesPanel, selected);

        return shipFilesPanel;
    }

    private static JPanel createLabelContainer(ShipCSVEntry selected) {
        Map<String, String> rowData = selected.getRowData();
        String shipName = rowData.get(StringConstants.NAME);
        String shipId = selected.getHullID();
        String hullFileName = selected.getHullFileName();

        Hull selectedHullFile = selected.getHullFile();
        Skin activeSkin = selected.getActiveSkin();
        Path skinFilePath = null;

        String spriteFileName = selectedHullFile.getSpriteName();
        String skinFileName = "";

        if (activeSkin != null && !activeSkin.isBase()) {
            shipName = activeSkin.getHullName();
            shipId = activeSkin.getSkinHullId();
            spriteFileName = activeSkin.getSpriteName();
            skinFilePath = activeSkin.getSkinFilePath();
            skinFileName = skinFilePath.getFileName().toString();
        }

        if (spriteFileName == null || spriteFileName.isEmpty()) {
            spriteFileName = selectedHullFile.getSpriteName();
        }

        Path shipFilePath = selectedHullFile.getShipFilePath();
        String shipFilePathName = shipFilePath.toString();

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);

        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));
        JLabel shipNameLabel = new JLabel("Ship name: " + shipName);
        shipNameLabel.setBorder(new EmptyBorder(ShipFilesSubpanel.createLabelInsets()));
        labelContainer.add(shipNameLabel);

        labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

        JLabel shipIDLabel = new JLabel("Ship ID: " + shipId);
        shipIDLabel.setBorder(new EmptyBorder(ShipFilesSubpanel.createLabelInsets()));
        labelContainer.add(shipIDLabel);

        labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

        JLabel hullFileNameLabel = new JLabel("Hull file : " + hullFileName);
        hullFileNameLabel.setToolTipText(shipFilePathName);
        hullFileNameLabel.setBorder(ShipFilesSubpanel.createLabelBorder());
        JPopupMenu hullContextMenu = ShipFilesSubpanel.createPathContextMenu(shipFilePath);
        hullFileNameLabel.addMouseListener(new MouseoverLabelListener(hullContextMenu, hullFileNameLabel));
        labelContainer.add(hullFileNameLabel);

        labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

        File spriteFile = FileUtilities.fetchDataFile(Path.of(spriteFileName),
                selected.getPackageFolder());
        JLabel spriteFileNameLabel = new JLabel("Sprite file: : " + spriteFile.getName());
        spriteFileNameLabel.setBorder(ShipFilesSubpanel.createLabelBorder());
        JPopupMenu spriteContextMenu = ShipFilesSubpanel.createPathContextMenu(spriteFile.toPath());
        spriteFileNameLabel.addMouseListener(new MouseoverLabelListener(spriteContextMenu, spriteFileNameLabel));
        spriteFileNameLabel.setToolTipText(spriteFile.toString());
        labelContainer.add(spriteFileNameLabel);

        if (!skinFileName.isEmpty()) {
            labelContainer.add(Box.createRigidArea(ShipFilesSubpanel.createPadding()));

            JLabel skinFileNameLabel = new JLabel("Skin file: " + skinFileName);
            skinFileNameLabel.setBorder(ShipFilesSubpanel.createLabelBorder());
            JPopupMenu skinContextMenu = ShipFilesSubpanel.createPathContextMenu(skinFilePath);
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

    private static Border createLabelBorder() {
        return new FlatLineBorder(ShipFilesSubpanel.createLabelInsets(), Color.LIGHT_GRAY);
    }

    private static Insets createLabelInsets() {
        return new Insets(0, 3, 2, 4);
    }

    private static JPopupMenu createPathContextMenu(Path filePath) {
        JPopupMenu openFileMenu = new JPopupMenu();
        JMenuItem openSourceFile = new JMenuItem(DataTreePanel.OPEN_SOURCE_FILE);
        openSourceFile.addActionListener(e -> FileUtilities.openPathInDesktop(filePath));
        openFileMenu.add(openSourceFile);
        JMenuItem openContainingFolder = new JMenuItem(DataTreePanel.OPEN_CONTAINING_FOLDER);
        openContainingFolder.addActionListener(e -> FileUtilities.openPathInDesktop(filePath.getParent()));
        openFileMenu.add(openContainingFolder);

        return openFileMenu;
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
            Map<String, String> rowData = entry.getRowData();
            String name = rowData.get("name");
            BufferedImage iconImage = FileUtilities.loadSprite(entry.fetchHullmodSpriteFile());
            JLabel imageLabel = Utility.getIconLabelWithBorder(new ImageIcon(iconImage));
            imageLabel.setToolTipText(name);
            hullmodsPanel.add(imageLabel);
        }

        panel.add(hullmodsPanel);
        panel.revalidate();
        panel.repaint();
    }

}
