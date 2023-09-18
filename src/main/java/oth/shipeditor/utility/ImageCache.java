package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.utility.text.StringValues;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 09.07.2023
 */
@Log4j2
public final class ImageCache {

    private static final ImageCache instance = new ImageCache();

    private final Map<File, BufferedImage> cache;

    private ImageCache() {
        cache = new HashMap<>();
    }

    public static BufferedImage loadImage(File file) {
        BufferedImage sprite = instance.cache.get(file);
        if (sprite != null) {
            log.info("Retrieved cached sprite: {}.", file.getName());
            return sprite;
        }
        try {
            sprite = ImageIO.read(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Image file loading failed: " + file,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            throw new UncheckedIOException("Failed to load sprite: " + file.getName(), ex);
        }
        log.info("Opening sprite: {}.", file.getName());
        instance.cache.put(file, sprite);
        return sprite;
    }

}
