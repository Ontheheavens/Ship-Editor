package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@Getter
public class GameDataRepository {

    /**
     * All ship entries by their hull IDs.
     */
    private final Map<String, ShipCSVEntry> allShipEntries;

    /**
     * All hullmod entries by their IDs.
     */
    private final Map<String, HullmodCSVEntry> allHullmodEntries;

    /**
     * All shipsystem entries by their IDs.
     */
    private final Map<String, ShipSystemCSVEntry> allShipsystemEntries;

    @Setter
    private Map<String, HullStyle> allHullStyles;

    @Setter
    private boolean shipDataLoaded;

    @Setter
    private boolean hullmodDataLoaded;

    @Setter
    private boolean shipsystemDataLoaded;

    public GameDataRepository() {
        this.allShipEntries = new HashMap<>();
        this.allHullmodEntries = new HashMap<>();
        this.allShipsystemEntries = new HashMap<>();
    }

}
