package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.Hull;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record HullFileOpened(Hull hull, String hullFileName) implements FileEvent {

}
