package oth.shipeditor.communication.events;

import oth.shipeditor.representation.data.Hull;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record HullFileOpened(Hull hull) implements BusEvent{

}
