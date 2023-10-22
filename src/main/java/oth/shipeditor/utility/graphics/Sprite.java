package oth.shipeditor.utility.graphics;

import lombok.Getter;
import oth.shipeditor.utility.Utility;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Convenience container for BufferedImage sprites, allows additional information such as path and filename.
 *
 * @author Ontheheavens
 * @since 30.07.2023
 */
@Getter
public final class Sprite {

    private final BufferedImage image;

    private final Path path;

    private final String filename;

    private String pathFromPackage;

    public Sprite(BufferedImage bufferedImage, Path inputPath, String shortFilename) {
        this.image = bufferedImage;
        this.path = inputPath;
        this.filename = shortFilename;
    }

    public String getPathFromPackage() {
        if (pathFromPackage == null || !pathFromPackage.isEmpty()) {
            pathFromPackage = Utility.computeRelativePathFromPackage(path);
        }
        return pathFromPackage;
    }

}
