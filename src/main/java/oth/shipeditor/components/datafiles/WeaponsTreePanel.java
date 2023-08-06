package oth.shipeditor.components.datafiles;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WeaponDataLoaded;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
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

    WeaponsTreePanel() {
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
                // Iterate through the weaponsByPackage map and convert each entry
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
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
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
