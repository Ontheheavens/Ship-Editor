package oth.shipeditor.communication.events;

import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record SpriteOpened(BufferedImage sprite) implements BusEvent {

}
