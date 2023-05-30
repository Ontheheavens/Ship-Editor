package oth.shipeditor.menubar;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.Window;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.HullFileOpened;
import oth.shipeditor.communication.events.SpriteOpened;
import oth.shipeditor.representation.data.Hull;

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
public class Files {

    private static File lastDirectory = null;

    private Files() {}

    /**
     * @return lambda that opens PNG file chooser.
     */
    public static Runnable createOpenSpriteAction() {
        return () -> {
            JFileChooser spriteChooser = new JFileChooser("C:\\Games\\Ship Editor\\src\\main\\resources");
            if (lastDirectory != null) {
                spriteChooser.setCurrentDirectory(lastDirectory);
            }
            FileNameExtensionFilter spriteFilter = new FileNameExtensionFilter(
                    "PNG Images", "png");
            spriteChooser.setFileFilter(spriteFilter);
            int returnVal = spriteChooser.showOpenDialog(Window.getFrame());
            lastDirectory = spriteChooser.getCurrentDirectory();
            Files.tryOpenSprite(returnVal,spriteChooser);
        };
    }

    public static void tryOpenSprite(int returnVal, JFileChooser spriteChooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = spriteChooser.getSelectedFile();
            try {
                BufferedImage sprite = ImageIO.read(file);
                EventBus.publish(new SpriteOpened(sprite));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            log.info("Opening: " + file.getName() + ".");
        } else {
            log.info("Open command cancelled by user.");
        }
    }

    public static Runnable createOpenHullFileAction() {
        return () -> {
            JFileChooser shipDataChooser = new JFileChooser("C:\\Games\\Ship Editor\\src\\main\\resources");
            if (lastDirectory != null) {
                shipDataChooser.setCurrentDirectory(lastDirectory);
            }
            FileNameExtensionFilter shipDataFilter = new FileNameExtensionFilter(
                    "JSON ship files", "ship");
            shipDataChooser.setFileFilter(shipDataFilter);
            int returnVal = shipDataChooser.showOpenDialog(Window.getFrame());
            lastDirectory = shipDataChooser.getCurrentDirectory();
            Files.tryOpenHullFile(returnVal, shipDataChooser);
        };
    }

    public static void tryOpenHullFile(int returnVal, JFileChooser shipDataChooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = shipDataChooser.getSelectedFile();
            EventBus.publish(new HullFileOpened(Files.loadHullFile(file)));
            log.info("Opening: " + file.getName() + ".");
        } else {
            log.info("Open command cancelled by user.");
        }
    }

    public static Hull loadHullFile(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, Hull.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
