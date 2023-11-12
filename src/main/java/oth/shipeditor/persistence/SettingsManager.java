package oth.shipeditor.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.GameDataRepository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
@Log4j2
public final class SettingsManager {

    @Getter @Setter
    private static Settings settings;

    @Getter
    private static final GameDataRepository gameData = new GameDataRepository();

    @Getter
    private static Path applicationDirectory;

    @Getter
    private static String coreFolderName;

    private static Path settingsFilePath;

    @Getter
    private static final String projectVersion = "0.7.2";

    private static GameDataPackage corePackage;

    private SettingsManager() {}

    static Settings createDefault() {
        Settings empty = new Settings();
        empty.setBackgroundColor(null);
        return empty;
    }

    static void setCoreFolderName(String folderName) {
        SettingsManager.coreFolderName = folderName;
        if (corePackage != null) {
            corePackage.setFolderName(folderName);
        }
    }

    static ObjectMapper getMapperForSettingsFile() {
        return FileUtilities.getConfigured();
    }

    /**
     * @return all directories in "mods" folder.
     * Caller is expected to do the filtering.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static List<Path> getAllModFolders() {
        List<Path> dataFolders = new ArrayList<>();
        try (Stream<Path> childDirectories = Files.list(Paths.get(settings.getModFolderPath()))) {
            childDirectories.filter(Files::isDirectory).forEach(dataFolders::add);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return dataFolders;
    }

    public static Path getCoreFolderPath() {
        return Path.of(settings.getCoreFolderPath());
    }

    public static boolean areFileErrorPopupsEnabled() {
        return settings.showLoadingErrors;
    }

    public static boolean isDataAutoloadEnabled() {
        return settings.loadDataAtStart;
    }

    static File getSettingsPath() {
        if (settingsFilePath != null) {
            return settingsFilePath.toFile();
        } else {
            Path workingDirectory = Paths.get("").toAbsolutePath();
            log.info("Default working directory: {}", workingDirectory);
            if (!workingDirectory.endsWith(PrimaryWindow.SHIP_EDITOR)) {
                log.error("Error while initializing settings: default directory has wrong filename.");
            }
            Path settingsPath = workingDirectory.resolve("ship_editor_settings.json");

            applicationDirectory = settingsPath.getParent();
            settingsFilePath = settingsPath;

            return settingsPath.toFile();
        }
    }

    public static void updateFileFromRuntime() {
        if (SettingsManager.settings == null) return;
        log.trace("Updating settings: getting path and mapper...");
        ObjectMapper mapper = SettingsManager.getMapperForSettingsFile();
        File settingsFile = SettingsManager.getSettingsPath();
        log.info("Updating settings: overwriting JSON file...");
        SettingsManager.writeSettingsToFile(mapper, settingsFile, SettingsManager.settings);
    }

    static void writeSettingsToFile(ObjectMapper mapper, File settingsFile, Settings writable) {
        try {
            mapper.writeValue(settingsFile, writable);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write settings file!", e);
        }
    }

    public static boolean isCoreFolder(GameDataPackage dataPackage) {
        String packageFolderName = dataPackage.getFolderName();
        return SettingsManager.isCoreFolder(packageFolderName);
    }

    public static boolean isCoreFolder(Path directory) {
        String folderName = directory.getFileName().toString();
        return SettingsManager.isCoreFolder(folderName);
    }

    public static boolean isCoreFolder(String folderName) {
        return folderName.equals(SettingsManager.getCoreFolderName());
    }

    @SuppressWarnings("NonThreadSafeLazyInitialization")
    public static GameDataPackage getCorePackage() {
        String corePackageName = SettingsManager.getCoreFolderName();
        if (corePackage == null) {
            corePackage = new GameDataPackage(corePackageName, false, false);
        }
        return corePackage;
    }

    public static <T extends CSVEntry> void announcePackages(Map<Path, List<T>> packages) {
        for (Map.Entry<Path, List<T>> entry : packages.entrySet()) {
            Path path = entry.getKey();
            if (!SettingsManager.isCoreFolder(path)) {
                settings.addDataPackage(path);
            }
        }
    }

}
