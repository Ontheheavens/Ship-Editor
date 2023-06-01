package oth.shipeditor.communication.events.files;

import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record SpriteOpened(BufferedImage sprite, String filename) implements FileEvent {

}
