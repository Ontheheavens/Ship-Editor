package oth.shipeditor.utility.graphics;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Convenience container for BufferedImage sprites, allows additional information such as path and filename.
 * @author Ontheheavens
 * @since 30.07.2023
 */
public record Sprite(BufferedImage image, Path path, String name) {}
