package oth.shipeditor.communication.events.files;

import oth.shipeditor.utility.graphics.Sprite;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record SpriteOpened(Sprite sprite) implements FileEvent {

}
