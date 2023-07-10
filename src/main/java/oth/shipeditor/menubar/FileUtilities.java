package oth.shipeditor.menubar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.parsing.JsonProcessor;
import oth.shipeditor.parsing.LoadHullmodDataAction;
import oth.shipeditor.parsing.LoadShipDataAction;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.ImageCache;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

    private static ShipLayer current;

    @Getter
    private static final Action loadShipDataAction = new LoadShipDataAction();

    @Getter
    private static final Action loadHullmodDataAction = new LoadHullmodDataAction();

    @Getter
    private static final Action openSpriteAction = new OpenSpriteAction();

    @Getter
    private static final Action openShipDataAction = new OpenHullAction();

    private FileUtilities() {
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void listenToLayerChange() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                current = checked.selected();
                FileUtilities.updateActionStates();
            }
            if (event instanceof ActiveLayerUpdated checked) {
                current = checked.updated();
                FileUtilities.updateActionStates();
            }
        });
    }

    private static void updateActionStates() {
        boolean spriteState = (current != null) && current.getShipSprite() == null && current.getShipData() == null;
        boolean hullState = (current != null) && current.getShipSprite() != null && current.getShipData() == null;
        openSpriteAction.setEnabled(spriteState);
        openShipDataAction.setEnabled(hullState);
    }

    private static Path searchFileInFolder(Path filePath, Path folderPath) {
        String fileName = filePath.getFileName().toString();

        try (var stream = Files.walk(folderPath)) {
            Optional<Path> first = stream.filter(file -> {
                        String toString = file.getFileName().toString();
                        return toString.equals(fileName);
                    }).findFirst();
            return first.orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void openPathInDesktop(Path toOpen) {
        FileUtilities.openPathInDesktop(toOpen.toFile());
    }

    private static void openPathInDesktop(File toOpen) {
        try {
            Desktop.getDesktop().open(toOpen);
        } catch (IOException ex) {
            log.error("Failed to open {} in Explorer!", toOpen);
        }
    }

    public static BufferedImage loadSprite(File file) {
        return ImageCache.loadImage(file);
    }

    /**
     * Searches for the input file, first in passed package folder, then in core data folder, then in mod folders.
     * @param filePath should be, for example, Path.of("graphics/icons/intel/investigation.png").
     * @param packageFolderPath supposed parent package, where search will start. Can be null.
     * @return fetched file if it exists, else NULL.
     */
    public static File fetchDataFile(Path filePath, Path packageFolderPath) {
        Path coreDataFolder = SettingsManager.getCoreFolderPath();
        List<Path> otherModFolders = SettingsManager.getAllModFolders();
        Path result = null;

        if (packageFolderPath != null) {
            // Search in parent mod package.
            result = FileUtilities.searchFileInFolder(filePath, packageFolderPath);
        }

        // If not found, search in core folder.
        if (result == null) {
            result = FileUtilities.searchFileInFolder(filePath, coreDataFolder);
        }
        if (result != null) {
            return result.toFile();
        }

        // If not found, search in other mods.
        for (Path modFolder : otherModFolders) {
            result = FileUtilities.searchFileInFolder(filePath, modFolder);
            if (result != null) {
                break;
            }
        }
        if (result == null) {
            log.error("Failed to fetch data file for {}!", filePath.getFileName());
        }
        if (result != null) {
            return result.toFile();
        }
        return null;
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
            hull.setShipFilePath(file.toPath());
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
            skin.setSkinFilePath(file.toPath());
        } catch (IOException e) {
            log.error(SKIN_FILE_LOADING_FAILED, file.getName());
            e.printStackTrace();
        }
        return skin;
    }

    private static class OpenHullAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser shipDataChooser = new JFileChooser("C:\\Games\\Ship Editor\\src\\main\\resources");
            if (lastDirectory != null) {
                shipDataChooser.setCurrentDirectory(lastDirectory);
            }
            FileNameExtensionFilter shipDataFilter = new FileNameExtensionFilter(
                    "JSON ship files", "ship");
            shipDataChooser.setFileFilter(shipDataFilter);
            int returnVal = shipDataChooser.showOpenDialog(null);
            lastDirectory = shipDataChooser.getCurrentDirectory();
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = shipDataChooser.getSelectedFile();
                Hull hull = FileUtilities.loadHullFile(file);
                EventBus.publish(new HullFileOpened(hull, file.getName()));
            } else {
                log.info(OPEN_COMMAND_CANCELLED_BY_USER);
            }
        }
    }

    private static class OpenSpriteAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser spriteChooser = new JFileChooser("C:\\Games\\Ship Editor\\src\\main\\resources");
            if (lastDirectory != null) {
                spriteChooser.setCurrentDirectory(lastDirectory);
            }
            FileNameExtensionFilter spriteFilter = new FileNameExtensionFilter(
                    "PNG Images", "png");
            spriteChooser.setFileFilter(spriteFilter);
            int returnVal = spriteChooser.showOpenDialog(null);
            lastDirectory = spriteChooser.getCurrentDirectory();
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = spriteChooser.getSelectedFile();
                BufferedImage sprite = FileUtilities.loadSprite(file);
                EventBus.publish(new SpriteOpened(sprite, file.getName()));
            } else {
                log.info(OPEN_COMMAND_CANCELLED_BY_USER);
            }
        }

    }

}
