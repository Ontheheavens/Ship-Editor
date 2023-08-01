package oth.shipeditor.parsing.loading;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.parsing.JsonProcessor;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.ImageCache;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@Log4j2
public final class FileLoading {

    static final String OPEN_COMMAND_CANCELLED_BY_USER = "Open command cancelled by user.";
    private static final String OPENING_SKIN_FILE = "Opening skin file: {}.";
    private static final String SKIN_FILE_LOADING_FAILED = "Skin file loading failed: {}";
    private static final String TRIED_TO_RESOLVE_SKIN_FILE_WITH_INVALID_EXTENSION = "Tried to resolve skin file with invalid extension!";

    static File lastDirectory;

    private FileLoading() {
    }

    public static ObjectMapper getConfigured() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        objectMapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        objectMapper.configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature(), true);
        return objectMapper;
    }

    private static Path searchFileInFolder(Path filePath, Path folderPath) {
        String fileName = filePath.getFileName().toString();

        try (var stream = Files.walk(folderPath)) {
            List<Path> foundFiles = stream.filter(file -> {
                String toString = file.getFileName().toString();
                return toString.equals(fileName);
            }).toList();
            for (Path foundFile : foundFiles) {
                String toString = foundFile.toString();
                if (toString.endsWith(filePath.toString())) {
                    return foundFile;
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage loadSpriteAsImage(File file) {
        return ImageCache.loadImage(file);
    }

    public static Sprite loadSprite(File file) {
        BufferedImage spriteImage = FileLoading.loadSpriteAsImage(file);

        Sprite sprite = new Sprite(spriteImage);
        sprite.setFileName(file.getName());
        sprite.setFilePath(file.toPath());
        return sprite;
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
            result = FileLoading.searchFileInFolder(filePath, packageFolderPath);
        }

        // If not found, search in core folder.
        if (result == null) {
            result = FileLoading.searchFileInFolder(filePath, coreDataFolder);
        }
        if (result != null) return result.toFile();

        // If not found, search in other mods.
        for (Path modFolder : otherModFolders) {
            result = FileLoading.searchFileInFolder(filePath, modFolder);
            if (result != null) {
                break;
            }
        }
        if (result == null) {
            log.error("Failed to fetch data file for {}!", filePath.getFileName());
        }
        if (result != null) return result.toFile();
        return null;
    }

    public static Hull loadHullFile(File file) {
        String toString = file.getPath();
        if (!toString.endsWith(".ship")) {
            throw new IllegalArgumentException("Tried to resolve hull file with invalid extension!");
        }
        Hull hull = null;
        try {
            ObjectMapper objectMapper = FileLoading.getConfigured();
            hull = objectMapper.readValue(file, Hull.class);
            hull.setShipFilePath(file.toPath());
            log.info("Opening hull file: {}", file.getName());
        } catch (IOException e) {
            log.error("Hull file loading failed: {}", file.getName());
            JOptionPane.showMessageDialog(null,
                    "Ship hull file loading failed, exception thrown at: " + file,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return hull;
    }

    static Skin loadSkinFile(File file) {
        String toString = file.getPath();
        if (!toString.endsWith(".skin")) {
            throw new IllegalArgumentException(FileLoading.TRIED_TO_RESOLVE_SKIN_FILE_WITH_INVALID_EXTENSION);
        }
        return FileLoading.parseSkinAsJSON(file);
    }

    private static Skin parseSkinAsJSON(File file) {
        Skin skin = null;
        ObjectMapper objectMapper = FileLoading.getConfigured();
        String preprocessed = JsonProcessor.correctJSON(file);
        try (JsonParser parser = objectMapper.createParser(preprocessed)) {
            log.info(FileLoading.OPENING_SKIN_FILE, file.getName());
            skin = objectMapper.readValue(parser, Skin.class);
            skin.setSkinFilePath(file.toPath());
        } catch (IOException e) {
            log.error(FileLoading.SKIN_FILE_LOADING_FAILED, file.getName());
            e.printStackTrace();
        }
        return skin;
    }

    public static void openHullAndDo(ActionListener action) {
        Path coreFolderPath = SettingsManager.getCoreFolderPath();
        JFileChooser shipDataChooser = new JFileChooser(coreFolderPath.toString());
        if (FileLoading.lastDirectory != null) {
            shipDataChooser.setCurrentDirectory(FileLoading.lastDirectory);
        }
        FileNameExtensionFilter shipDataFilter = new FileNameExtensionFilter(
                "JSON ship files", "ship");
        shipDataChooser.setFileFilter(shipDataFilter);
        int returnVal = shipDataChooser.showOpenDialog(null);
        FileLoading.lastDirectory = shipDataChooser.getCurrentDirectory();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ActionEvent event = new ActionEvent(shipDataChooser, ActionEvent.ACTION_PERFORMED, null);
            action.actionPerformed(event);
        }
        else {
            log.info(FileLoading.OPEN_COMMAND_CANCELLED_BY_USER);
        }
    }

    /**
     * Target CSV file is expected to have a header row and an ID column designated in said header.
     * @param path address of the target file.
     * @return List of rows where each row is a Map of string keys and string values.
     */
    static List<Map<String, String>> parseCSVTable(Path path) {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.configure(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE, true);

        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        File csvFile = path.toFile();
        List<Map<String, String>> csvData = new ArrayList<>();
        try (MappingIterator<Map<String, String>> iterator = csvMapper.readerFor(Map.class)
                .with(csvSchema)
                .readValues(csvFile)) {
            while (iterator.hasNext()) {
                Map<String, String> row = iterator.next();
                String id = row.get(StringConstants.ID);
                String name = row.get("name");
                if (id != null && !id.isEmpty() && !name.startsWith("#")) {
                    // We are skipping a row if ID is missing or if row is commented out.
                    csvData.add(row);
                }
            }
        } catch (IOException exception) {
            log.error("Data CSV loading failed!");
            exception.printStackTrace();
        }
        return csvData;
    }

}
