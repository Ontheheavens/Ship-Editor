package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.ShipSystemsLoaded;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 02.08.2023
 */
@Log4j2
public
class ShipSystemsTreePanel extends CSVDataTreePanel<ShipSystemCSVEntry>{

    public ShipSystemsTreePanel() {
        super("Shipsystem files");
    }

    @Override
    protected Action getLoadDataAction() {
        return FileLoading.getLoadShipSystemDataAction();
    }

    @Override
    protected String getEntryTypeName() {
        return "shipsystem";
    }

    @Override
    protected Map<String, ShipSystemCSVEntry> getRepository() {
        GameDataRepository gameData = SettingsManager.getGameData();
        return gameData.getAllShipsystemEntries();
    }

    @Override
    protected void setLoadedStatus() {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setShipsystemDataLoaded(true);
    }


    @Override
    protected void initWalkerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipSystemsLoaded checked) {
                Map<String, List<ShipSystemCSVEntry>> shipsystems = checked.systemsByPackage();
                if (shipsystems == null) {
                    throw new RuntimeException("Shipsystem data initialization failed: table data is NULL!");
                }
                populateEntries(shipsystems);
            }
        });
    }


    @Override
    protected void updateEntryPanel(ShipSystemCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);
    }

    @Override
    protected ShipSystemCSVEntry getObjectFromNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (!(userObject instanceof ShipSystemCSVEntry checked)) return null;
        return checked;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if(entry instanceof ShipSystemCSVEntry checked) {
            return "<html>" +
                    "<p>" + "Shipsystem ID: " + checked.getShipSystemID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    protected Class<?> getEntryClass() {
        return ShipSystemCSVEntry.class;
    }

}
