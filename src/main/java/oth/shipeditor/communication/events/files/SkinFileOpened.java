package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.SkinSpecFile;

/**
 * @author Ontheheavens
 * @since 01.07.2023
 */
public record SkinFileOpened(SkinSpecFile skinSpecFile) implements FileEvent {

}
