package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.ShipSystemsLoaded;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 01.08.2023
 */
@Log4j2
public class LoadShipSystemDataAction extends LoadCSVDataAction<ShipSystemCSVEntry> {

    public LoadShipSystemDataAction() {
        super(Paths.get("data", "shipsystems", "ship_systems.csv"));
    }

    @Override
    protected void publishResult(Map<String, List<ShipSystemCSVEntry>> entriesByPackage) {
        EventBus.publish(new ShipSystemsLoaded(entriesByPackage));
    }

    @Override
    protected ShipSystemCSVEntry instantiateEntry(Map<String, String> row, Path folderPath, Path dataFilePath) {
        return new ShipSystemCSVEntry(row, folderPath, dataFilePath);
    }

}
