package oth.shipeditor.menubar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.components.datafiles.LoadGameDataAction;
import oth.shipeditor.parsing.JsonProcessor;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
@Log4j2
public final class FileUtilities {

    private static final String OPEN_COMMAND_CANCELLED_BY_USER = "Open command cancelled by user.";
    public static final String STARSECTOR_CORE = "starsector-core";
    private static final String OPENING_SKIN_FILE = "Opening skin file: {}.";
    private static final String SKIN_FILE_LOADING_FAILED = "Skin file loading failed: {}";
    private static final String TRIED_TO_RESOLVE_SKIN_FILE_WITH_INVALID_EXTENSION = "Tried to resolve skin file with invalid extension!";
    private static File lastDirectory;

    @Getter
    private static final Action loadGameDataAction = new LoadGameDataAction();

    private FileUtilities() {}

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
            FileUtilities.tryOpenSprite(returnVal,spriteChooser);
        };
    }

    private static void tryOpenSprite(int returnVal, JFileChooser spriteChooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = spriteChooser.getSelectedFile();
            BufferedImage sprite = FileUtilities.loadSprite(file);
            EventBus.publish(new SpriteOpened(sprite, file.getName()));
        } else {
            log.info(OPEN_COMMAND_CANCELLED_BY_USER);
        }
    }

    public static BufferedImage loadSprite(File file) {
        BufferedImage sprite;
        try {
            sprite = ImageIO.read(file);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load sprite: " + file.getName(), ex);
        }
        log.info("Opening sprite: {}.", file.getName());
        return sprite;
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
            FileUtilities.tryOpenHullFile(returnVal, shipDataChooser);
        };
    }

    private static void tryOpenHullFile(int returnVal, JFileChooser shipDataChooser) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = shipDataChooser.getSelectedFile();
            Hull hull = FileUtilities.loadHullFile(file);
            EventBus.publish(new HullFileOpened(hull, file.getName()));
        } else {
            log.info(OPEN_COMMAND_CANCELLED_BY_USER);
        }
    }

    private static ObjectMapper getConfigured() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        objectMapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        objectMapper.configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature(), true);
        return objectMapper;
    }

    public static Hull loadHullFile(File file) {
        String toString = file.getPath();
        if (!toString.endsWith(".ship")) {
            throw new IllegalArgumentException("Tried to resolve hull file with invalid extension!");
        }
        Hull hull = null;
        try {
            ObjectMapper objectMapper = FileUtilities.getConfigured();
            hull = objectMapper.readValue(file, Hull.class);
            log.info("Opening hull file: {}", file.getName());
        } catch (IOException e) {
            log.error("Hull file loading failed: {}", file.getName());
            e.printStackTrace();
        }
        return hull;
    }

    public static Skin loadSkinFile(File file) {
        String toString = file.getPath();
        if (!toString.endsWith(".skin")) {
            throw new IllegalArgumentException(TRIED_TO_RESOLVE_SKIN_FILE_WITH_INVALID_EXTENSION);
        }
        return FileUtilities.parseSkinAsJSON(file);
    }

    private static Skin parseSkinAsJSON(File file) {
        Skin skin = null;
        ObjectMapper objectMapper = FileUtilities.getConfigured();
        String preprocessed = JsonProcessor.correctJSON(file);
        try (JsonParser parser = objectMapper.createParser(preprocessed)) {
            log.info(OPENING_SKIN_FILE, file.getName());
            skin = objectMapper.readValue(parser, Skin.class);
        } catch (IOException e) {
            log.error(SKIN_FILE_LOADING_FAILED, file.getName());
            e.printStackTrace();
        }
        return skin;
    }

}
