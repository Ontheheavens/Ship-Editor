package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.Skin;

/**
 * @author Ontheheavens
 * @since 01.07.2023
 */
public record SkinFileOpened(Skin skin, String skinFileName) implements FileEvent {

}
