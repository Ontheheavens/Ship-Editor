package oth.shipeditor.menubar;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.representation.Hull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
@Log4j2
public final class Files {

    private static File lastDirectory = null;

    private Files() {}

    /**
     * @return lambda that opens PNG file chooser.
     */
    static Runnable createOpenSpriteAction() {
        return () -> {
            JFileChooser spriteChooser = new JFileChooser("C:\\Games\\Ship Editor\\src\\main\\resources");
            if (lastDirectory != null) {
                spriteChooser.setCurrentDirectory(lastDirectory);
            }
            FileNameExtensionFilter spriteFilter = new FileNameExtensionFilter(
                    "PNG Images", "png");
            spriteChooser.setFileFilter(spriteFilter);
            int returnVal = spriteChooser.showOpenDialog(null);
            lastDirectory = spriteChooser.getCurrentDirectory();
            Files.tryOpenSprite(returnVal,spriteChooser);
        };
    }

    private static void tryOpenSprite(int returnVal, JFileChooser spriteChooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = spriteChooser.getSelectedFile();
            Files.loadSprite(file);
        } else {
            log.info("Open command cancelled by user.");
        }
    }

    public static void loadSprite(File file) {
        try {
            BufferedImage sprite = ImageIO.read(file);
            EventBus.publish(new SpriteOpened(sprite, file.getName()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        log.info("Opening: " + file.getName() + ".");
    }

    static Runnable createOpenHullFileAction() {
        return () -> {
            JFileChooser shipDataChooser = new JFileChooser("C:\\Games\\Ship Editor\\src\\main\\resources");
            if (lastDirectory != null) {
                shipDataChooser.setCurrentDirectory(lastDirectory);
            }
            FileNameExtensionFilter shipDataFilter = new FileNameExtensionFilter(
                    "JSON ship files", "ship");
            shipDataChooser.setFileFilter(shipDataFilter);
            int returnVal = shipDataChooser.showOpenDialog(null);
            lastDirectory = shipDataChooser.getCurrentDirectory();
            Files.tryOpenHullFile(returnVal, shipDataChooser);
        };
    }

    private static void tryOpenHullFile(int returnVal, JFileChooser shipDataChooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = shipDataChooser.getSelectedFile();
            Files.loadHullFile(file);
        } else {
            log.info("Open command cancelled by user.");
        }
    }

    public static void loadHullFile(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Hull hull = objectMapper.readValue(file, Hull.class);
            EventBus.publish(new HullFileOpened(hull, file.getName()));
            log.info("Opening: {}.", file.getName());
        } catch (IOException e) {
            log.error("Hull file loading failed!");
            e.printStackTrace();
        }

    }

}
