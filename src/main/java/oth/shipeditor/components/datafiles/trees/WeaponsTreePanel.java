package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WeaponDataLoaded;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
public class WeaponsTreePanel extends CSVDataTreePanel<WeaponCSVEntry>{

    public WeaponsTreePanel() {
        super("Weapon files");
    }

    @Override
    protected Action getLoadDataAction() {
        return FileUtilities.getLoadWeaponDataAction();
    }

    @Override
    protected String getEntryTypeName() {
        return "weapon";
    }

    @Override
    protected Map<String, WeaponCSVEntry> getRepository() {
        GameDataRepository gameData = SettingsManager.getGameData();
        return gameData.getAllWeaponEntries();
    }

    @Override
    protected void setLoadedStatus() {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setWeaponsDataLoaded(true);
    }

    @Override
    protected void initWalkerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof WeaponDataLoaded checked) {
                Map<Path, Map<String, WeaponCSVEntry>> weaponsByPackage = checked.weaponsByPackage();

                Map<String, List<WeaponCSVEntry>> weaponPackageList = new HashMap<>();
                for (Map.Entry<Path, Map<String, WeaponCSVEntry>> packageEntry : weaponsByPackage.entrySet()) {
                    Path packageEntryKey = packageEntry.getKey();
                    String packageName = packageEntryKey.toString();
                    Map<String, WeaponCSVEntry> weaponMap = packageEntry.getValue();

                    List<WeaponCSVEntry> weaponCSVEntries = new ArrayList<>(weaponMap.values());

                    weaponPackageList.put(packageName, weaponCSVEntries);
                }

                populateEntries(weaponPackageList);
            }
        });
    }

    @Override
    protected void updateEntryPanel(WeaponCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();

        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 0, 5);
        JPanel specFilePanel = new JPanel();
        specFilePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        ComponentUtilities.outfitPanelWithTitle(specFilePanel, new Insets(1, 0, 0, 0),
                StringValues.FILES);
        specFilePanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(2, 0, 0, 0));
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));

        labelContainer.add(WeaponsTreePanel.createWeaponFileLabel(selected.getSpecFile()));

        specFilePanel.add(labelContainer);
        rightPanel.add(specFilePanel, constraints);

        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    private static JLabel createWeaponFileLabel(WeaponSpecFile weaponSpecFile) {
        Path weaponSpecFilePath = weaponSpecFile.getWeaponSpecFilePath();
        JLabel label = new JLabel("Weapon file : " + weaponSpecFilePath.getFileName());
        label.setToolTipText(String.valueOf(weaponSpecFilePath));
        label.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        JPopupMenu pathContextMenu = ComponentUtilities.createPathContextMenu(weaponSpecFilePath);
        label.addMouseListener(new MouseoverLabelListener(pathContextMenu, label));
        return label;
    }

    @Override
    protected WeaponCSVEntry getObjectFromNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (!(userObject instanceof WeaponCSVEntry checked)) return null;
        return checked;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if(entry instanceof WeaponCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Weapon ID: " + checked.getWeaponID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    protected Class<?> getEntryClass() {
        return WeaponCSVEntry.class;
    }

}
