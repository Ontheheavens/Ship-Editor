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
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.LoadingActionFired;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.parsing.JsonProcessor;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.representation.weapon.ProjectileSpecFile;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.Errors;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.ImageCache;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@SuppressWarnings({"CallToPrintStackTrace", "ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyCoupledClass"})
@Log4j2
public final class FileLoading {

    @Getter
    private static final DataLoadingAction loadShips = new LoadShipDataAction();
    @Getter
    private static final DataLoadingAction loadHullmods = new LoadHullmodDataAction();
    @Getter
    private static final DataLoadingAction loadHullStyles = new LoadHullStyleDataAction();
    @Getter
    private static final DataLoadingAction loadEngineStyles = new LoadEngineStyleDataAction();
    @Getter
    private static final DataLoadingAction loadShipSystems = new LoadShipSystemDataAction();
    @Getter
    private static final DataLoadingAction loadWings = new LoadWingDataAction();
    @Getter
    private static final DataLoadingAction loadWeapons = new LoadWeaponsDataAction();
    @Getter
    public static final Action openSprite = new OpenSpriteAction();
    @Getter
    public static final Action openShip = new OpenHullAction();
    @Getter
    private static final Action loadHullAsLayer = new LoadHullAsLayer();

    private FileLoading() {}

    /**
     * To be called only after all components and settings have been initialized.
     */
    public static CompletableFuture<List<Runnable>> loadGameData() {
        EventBus.publish(new LoadingActionFired(true));
        Collection<DataLoadingAction> loadActions = new ArrayList<>();
        loadActions.add(loadShips);
        loadActions.add(loadHullmods);
        loadActions.add(loadHullStyles);
        loadActions.add(loadEngineStyles);
        loadActions.add(loadShipSystems);
        loadActions.add(loadWings);
        loadActions.add(loadWeapons);

        List<CompletableFuture<Runnable>> futures = new ArrayList<>();
        for (DataLoadingAction action : loadActions) {
            CompletableFuture<Runnable> future = CompletableFuture.supplyAsync(action::perform);
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Runnable>> allResults = allOf.thenApply(v ->
                futures.stream().map(CompletableFuture::join).toList()
        );

        allResults.thenAccept(runnables -> runnables.forEach(Runnable::run));
        allResults.thenRun(() -> EventBus.publish(new LoadingActionFired(false)));

        return allResults;
    }

    /**
     * @param loadAction executed with a separate GUI callback.
     */
    public static Action loadDataAsync(DataLoadingAction loadAction) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventBus.publish(new LoadingActionFired(true));
                CompletableFuture<Runnable> loadResult = CompletableFuture.supplyAsync(loadAction::perform);
                CompletableFuture<Void> publishResult = loadResult.thenAccept(Runnable::run);
                publishResult.thenRun(() -> EventBus.publish(new LoadingActionFired(false)));
            }
        };
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

    @SuppressWarnings("NestedTryStatement")
    public static BufferedImage loadImageResource(String imageFilename) {
        Class<FileLoading> loadingClass = FileLoading.class;
        ClassLoader classLoader = loadingClass.getClassLoader();

        URL spritePath = Objects.requireNonNull(classLoader.getResource(imageFilename));
        File spriteFile;
        try {
            URI pathURI = spritePath.toURI();
            if (pathURI.isOpaque()) {
                try ( InputStream inputStream = loadingClass.getResourceAsStream("/" + imageFilename)) {
                    if (inputStream != null) {
                        return ImageIO.read(inputStream);
                    } else {
                        throw new RuntimeException("Resource not found!");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            spriteFile = new File(pathURI);
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(null,
                    "Image resource loading failed, exception thrown at: " + spritePath,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            return null;
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
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public static File fetchDataFile(Path filePath, Path packageFolderPath) {
        if (filePath == null) {
            log.error("Failed to fetch data file, input path is null.");
            return null;
        }
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

    static HullSpecFile loadHullFile(File file) {
        HullSpecFile hullSpecFile = FileLoading.loadDataFile(file, ".ship", HullSpecFile.class);
        if (hullSpecFile != null) {
            hullSpecFile.setFilePath(file.toPath());
            GameDataRepository.putSpec(hullSpecFile);
        }
        return hullSpecFile;
    }

    static WeaponSpecFile loadWeaponFile(File file) {
        WeaponSpecFile weaponSpecFile = FileLoading.loadDataFile(file, ".wpn", WeaponSpecFile.class);
        if (weaponSpecFile != null) {
            weaponSpecFile.setWeaponSpecFilePath(file.toPath());
        }
        return weaponSpecFile;
    }

    static SkinSpecFile loadSkinFile(File file) {
        SkinSpecFile skinSpecFile = FileLoading.loadDataFile(file, ".skin", SkinSpecFile.class);
        if (skinSpecFile != null) {
            skinSpecFile.setFilePath(file.toPath());
            GameDataRepository.putSpec(skinSpecFile);
        }
        return skinSpecFile;
    }

    static VariantFile loadVariantFile(File file) {
        VariantFile variantFile = FileLoading.loadDataFile(file, StringConstants.VARIANT_EXTENSION, VariantFile.class);
        if (variantFile != null) {
            variantFile.setVariantFilePath(file.toPath());
        }
        return variantFile;
    }

    static ProjectileSpecFile loadProjectileFile(File file) {
        ProjectileSpecFile projectileFile = FileLoading.loadDataFile(file, ".proj", ProjectileSpecFile.class);
        if (projectileFile != null) {
            projectileFile.setProjectileSpecFilePath(file.toPath());
        }
        return projectileFile;
    }

    private static <T> T loadDataFile(File file, String extension, Class<T> dataClass) {
        String toString = file.getPath();
        if (!toString.endsWith(extension)) {
            throw new IllegalArgumentException("Tried to resolve data file with invalid extension!");
        }

        T dataFile;
        try {
            ObjectMapper objectMapper = FileUtilities.getConfigured();
            log.trace("Opening data file: {}", file.getName());
            dataFile = objectMapper.readValue(file, dataClass);
        } catch (IOException e) {
            log.error("Data file parsing failed, retrying with correction: {}", file.getName());

            dataFile = FileLoading.parseCorrectableJSON(file, dataClass);
            if (dataFile == null) {
                log.error("Data file parsing failed conclusively: {}", file.getName());
                e.printStackTrace();
                if (SettingsManager.areFileErrorPopupsEnabled()) {
                    Errors.showFileError("Data file parsing failed, exception thrown at: " + file);
                }
            }
        }
        return dataFile;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static <T> T parseCorrectableJSON(File file, Class<T> target) {
        ObjectMapper objectMapper = FileUtilities.getConfigured();

        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType javaType = typeFactory.constructType(target);

        return FileLoading.parseCorrectableJSON(file, javaType);
    }

    @SuppressWarnings("AssignmentToNull")
    static <T> T parseCorrectableJSON(File file, JavaType targetType) {
        T result;
        ObjectMapper objectMapper = FileUtilities.getConfigured();
        objectMapper.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);

        String content = JsonProcessor.straightenMalformed(file);
        try (JsonParser parser = objectMapper.createParser(content)) {
            result = objectMapper.readValue(parser, targetType);
        } catch (IOException e) {
            log.error("Corrected JSON parsing failed: {}", file.getName());
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    static void openHullAndDo(ActionListener action) {
        JFileChooser shipDataChooser = FileUtilities.getHullFileChooser();
        int returnVal = shipDataChooser.showOpenDialog(null);
        FileUtilities.lastDirectory = shipDataChooser.getCurrentDirectory();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ActionEvent event = new ActionEvent(shipDataChooser, ActionEvent.ACTION_PERFORMED, null);
            action.actionPerformed(event);
        }
        else {
            log.info(FileUtilities.OPEN_COMMAND_CANCELLED_BY_USER);
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
        } catch (Exception exception) {
            log.error("Data CSV loading failed!");
            exception.printStackTrace();
            if (SettingsManager.areFileErrorPopupsEnabled()) {
                Errors.showFileError("Failed to parse CSV table (likely semantic errors), " +
                        "loading incomplete: " + csvFile);
            }
            return csvData;
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

    private static class OpenHullAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileLoading.openHullAndDo(event -> {
                    JFileChooser shipDataChooser = (JFileChooser) event.getSource();
                    File file = shipDataChooser.getSelectedFile();
                    HullSpecFile hullSpecFile = FileLoading.loadHullFile(file);
                    if (hullSpecFile != null) {
                        EventBus.publish(new HullFileOpened(hullSpecFile, file.getName()));
                    } else {
                        log.error(StringValues.FAILURE_TO_LOAD_HULL_CANCELLING_ACTION, file);
                        JOptionPane.showMessageDialog(null,
                                StringValues.FAILURE_TO_LOAD_HULL_CANCELLING_ACTION_ALT + file,
                                StringValues.FILE_LOADING_ERROR,
                                JOptionPane.ERROR_MESSAGE);
                    }
            });
        }
    }

}
