package oth.shipeditor.representation;

import lombok.Getter;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
public class GameDataRepository {

    /**
     * All ship entries by their hull IDs.
     */
    @Getter
    private final Map<String, ShipCSVEntry> allShipEntries;

    /**
     * All hullmod entries by their IDs.
     */
    @Getter
    private final Map<String, HullmodCSVEntry> allHullmodEntries;

    public GameDataRepository() {
        this.allShipEntries = new HashMap<>();
        this.allHullmodEntries = new HashMap<>();
    }



}
