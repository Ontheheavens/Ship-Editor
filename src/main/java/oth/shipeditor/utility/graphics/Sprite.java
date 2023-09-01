package oth.shipeditor.utility.graphics;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Convenience container for BufferedImage sprites, allows additional information such as path and filename.
 * @author Ontheheavens
 * @since 30.07.2023
 */
@Getter @Setter
public class Sprite {

    private final BufferedImage spriteImage;

    private Path filePath;

    private String fileName;

    public Sprite(BufferedImage image) {
        this.spriteImage = image;
    }

}
