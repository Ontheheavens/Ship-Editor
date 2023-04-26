package oth.shipeditor.components;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.CoordinatePlanePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Encapsulates a ship sprite, loaded from .png file and displayed in the application.
 * Rough draft, needs proper refactoring later.
 */
public class ShipSprite implements Paintable {

    private final BufferedImage image;
    private final CoordinatePlanePanel parent;

    @Getter @Setter
    private double scale = 1;

    public ShipSprite(String filepath, CoordinatePlanePanel parent) {
        this.parent = parent;
        try {
            image = ImageIO.read(new File(filepath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sprite at " + filepath);
        }
    }

    public int getImageWidth() {
        return (int) (image.getWidth() * scale);
    }

    public int getImageHeight() {
        return (int) (image.getHeight() * scale);
    }

    /**
     * @return Point that corresponds to the top-left corner of ship sprite.
     */
    public Point getDefaultImageAnchor() {
        Point coordinateCenter = parent.getCoordinateCenter();
        return new Point(coordinateCenter.x - (getImageWidth() / 2), coordinateCenter.y - (getImageHeight() / 2));
    }

    @Override
    public void paint(Graphics input) {
        // Size calculations needed for proper centering.
        int imageWidth = getImageWidth();
        int imageHeight = getImageHeight();
        Point anchor = getDefaultImageAnchor();
        Graphics2D graphics2D = (Graphics2D) input.create();
        graphics2D.drawImage(image, anchor.x, anchor.y, imageWidth, imageHeight, null);

        // Image boundaries
        graphics2D.drawLine(anchor.x, anchor.y, anchor.x + imageWidth, anchor.y);
        graphics2D.drawLine(anchor.x + imageWidth, anchor.y, anchor.x + imageWidth, anchor.y + imageHeight);
        graphics2D.drawLine(anchor.x + imageWidth, anchor.y + imageHeight, anchor.x, anchor.y + imageHeight);
        graphics2D.drawLine(anchor.x, anchor.y + imageHeight, anchor.x, anchor.y);

        graphics2D.dispose();
    }


}
