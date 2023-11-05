package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.ship.HullSpecFile;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record HullFileOpened(HullSpecFile hullSpecFile, String hullFileName) implements FileEvent {

}
