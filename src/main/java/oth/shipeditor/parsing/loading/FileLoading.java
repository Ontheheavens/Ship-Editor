package oth.shipeditor.parsing.loading;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.parsing.JsonProcessor;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@Log4j2
public final class FileLoading {

    static final String OPEN_COMMAND_CANCELLED_BY_USER = "Open command cancelled by user.";

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        mapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature(), true);
    }

    static File lastDirectory;

    private FileLoading() {
    }

    static ObjectMapper getConfigured() {
        return mapper;
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

    public static BufferedImage loadImageResource(String imageFilename) {
        Class<FileLoading> loadingClass = FileLoading.class;
        ClassLoader classLoader = loadingClass.getClassLoader();

        URL spritePath = Objects.requireNonNull(classLoader.getResource(imageFilename));
        File spriteFile;
        try {
            spriteFile = new File(spritePath.toURI());
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(null,
                    "Image resource loading failed, exception thrown at: " + spritePath,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        return FileLoading.loadSpriteAsImage(spriteFile);
    }

    public static BufferedImage loadSpriteAsImage(File file) {
        return ImageCache.loadImage(file);
    }

    public static Sprite loadSprite(File file) {
        BufferedImage spriteImage = FileLoading.loadSpriteAsImage(file);
        String name = file.getName();
        Path path = file.toPath();
        return new Sprite(spriteImage, path, name);
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

        if (result != null) {
            return result.toFile();
        } else {
            log.error("Failed to fetch data file for {}!", filePath.getFileName());
        }
        return null;
    }

    public static HullSpecFile loadHullFile(File file) {
        HullSpecFile hullSpecFile = FileLoading.loadDataFile(file, ".ship", HullSpecFile.class);
        hullSpecFile.setFilePath(file.toPath());
        GameDataRepository.putSpec(hullSpecFile);
        return hullSpecFile;
    }

    static WeaponSpecFile loadWeaponFile(File file) {
        WeaponSpecFile weaponSpecFile = FileLoading.loadDataFile(file, ".wpn", WeaponSpecFile.class);
        weaponSpecFile.setWeaponSpecFilePath(file.toPath());
        return weaponSpecFile;
    }

    static SkinSpecFile loadSkinFile(File file) {
        SkinSpecFile skinSpecFile = FileLoading.loadDataFile(file, ".skin", SkinSpecFile.class);
        skinSpecFile.setFilePath(file.toPath());
        GameDataRepository.putSpec(skinSpecFile);
        return skinSpecFile;
    }

    static VariantFile loadVariantFile(File file) {
        VariantFile variantFile = FileLoading.loadDataFile(file, ".variant", VariantFile.class);
        variantFile.setVariantFilePath(file.toPath());
        return variantFile;
    }

    private static <T> T loadDataFile(File file, String extension, Class<T> dataClass) {
        String toString = file.getPath();
        if (!toString.endsWith(extension)) {
            throw new IllegalArgumentException("Tried to resolve data file with invalid extension!");
        }

        T dataFile;
        try {
            ObjectMapper objectMapper = FileLoading.getConfigured();
            log.info("Opening data file: {}", file.getName());
            dataFile = objectMapper.readValue(file, dataClass);
        } catch (IOException e) {
            log.error("Data file parsing failed, retrying with correction: {}", file.getName());

            dataFile = FileLoading.parseCorrectableJSON(file, dataClass);
            if (dataFile == null) {
                log.error("Data file parsing failed conclusively: {}", file.getName());
                JOptionPane.showMessageDialog(null,
                        "Data file parsing failed, exception thrown at: " + file,
                        StringValues.FILE_LOADING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                throw new RuntimeException("Data file parsing failed conclusively!", e);
            }
        }
        return dataFile;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static <T> T parseCorrectableJSON(File file, Class<T> target) {
        ObjectMapper objectMapper = FileLoading.getConfigured();

        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType javaType = typeFactory.constructType(target);

        return FileLoading.parseCorrectableJSON(file, javaType);
    }

    static <T> T parseCorrectableJSON(File file, JavaType targetType) {
        T result = null;
        ObjectMapper objectMapper = FileLoading.getConfigured();
        objectMapper.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);

        String content = JsonProcessor.straightenMalformed(file);
        try (JsonParser parser = objectMapper.createParser(content)) {
            result = objectMapper.readValue(parser, targetType);
        } catch (IOException e) {
            log.error("Corrected JSON parsing failed: {}", file.getName());
            e.printStackTrace();
        }
        return result;
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

    @SuppressWarnings("WeakerAccess")
    public static List<File> fetchFilesWithExtension(Path target, String dotlessExtension) {
        List<File> files = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(target)) {
            pathStream.filter(path -> {
                        String toString = path.getFileName().toString();
                        return toString.endsWith("." + dotlessExtension);
                    })
                    .map(Path::toFile)
                    .forEach(files::add);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return files;
    }

    static List<Map<String, String>> parseCSVTable(Path path) {
        return FileLoading.parseCSVTable(path, FileLoading.getNormalValidationPredicate());
    }

    /**
     * Target CSV file is expected to have a header row and an ID column designated in said header.
     * @param path address of the target file.
     * @return List of rows where each row is a Map of string keys and string values.
     */
    static List<Map<String, String>> parseCSVTable(Path path, Predicate<Map<String, String>> validationPredicate) {
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
                if (validationPredicate.test(row)) {
                    csvData.add(row);
                }
            }
        } catch (IOException exception) {
            log.error("Data CSV loading failed!");
            exception.printStackTrace();
        }
        return csvData;
    }

    private static Predicate<Map<String, String>> getNormalValidationPredicate() {
        return row -> {
            String id = row.get(StringConstants.ID);
            String name = row.get("name");
            boolean validID = id != null && !id.isEmpty();
            return validID && !name.startsWith("#");
        };
    }

    static Predicate<Map<String, String>> getWingValidationPredicate() {
        return row -> {
            String id = row.get(StringConstants.ID);
            boolean validID = id != null && !id.isEmpty();
            return validID && !id.startsWith("#");
        };
    }

}
