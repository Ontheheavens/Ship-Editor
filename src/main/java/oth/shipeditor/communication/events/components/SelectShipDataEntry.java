package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;

/**
 * @author Ontheheavens
 * @since 06.10.2023
 */
public record SelectShipDataEntry(ShipCSVEntry entry) implements ComponentEvent{

}
