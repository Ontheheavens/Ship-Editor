package oth.shipeditor.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
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

    private SettingsManager() {}

    static Settings createDefault() {
        Settings empty = new Settings();
        empty.setBackgroundColor(null);
        return empty;
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

    static File getSettingsPath() {
        Path workingDirectory = Paths.get("").toAbsolutePath();
        log.info("Default working directory: {}", workingDirectory);
        if (!workingDirectory.endsWith(PrimaryWindow.SHIP_EDITOR)) {
            log.error("Error while initializing settings: default directory has wrong name.");
        }
        Path settingsPath = workingDirectory.resolve("ship_editor_settings.json");
        return settingsPath.toFile();
    }

    static void updateFileFromRuntime() {
        if (SettingsManager.settings == null) return;
        log.info("Updating settings: getting path and mapper...");
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

}
