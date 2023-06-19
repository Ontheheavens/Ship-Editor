package oth.shipeditor.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
@Log4j2
public final class SettingsManager {

    @Getter @Setter
    private static Settings settings;

    private SettingsManager() {}

    private static Settings createDefault() {
        Settings empty = new Settings();
        Color gray = Color.GRAY;
        empty.setBackgroundColor(gray);
        return empty;
    }

    public static void initialize() {
        Path workingDirectory = Paths.get("").toAbsolutePath();
        log.info("Current folder: {}", workingDirectory);
        if (!workingDirectory.endsWith(PrimaryWindow.SHIP_EDITOR)) {
            throw new IllegalStateException("Failed to initialize settings: wrong directory!");
        }
        Path settingsPath = workingDirectory.resolve("ship_editor_settings.json");
        ObjectMapper mapper = new ObjectMapper();
        Settings loaded;
        File settingsFile = settingsPath.toFile();
        try {
            if (settingsFile.exists()) {
                log.info("Reading existing settings file..");
                loaded = mapper.readValue(settingsFile, Settings.class);
                if (loaded != null) {
                    log.info("Settings read successful.");
                }
            } else {
                log.info("Settings file not found, creating default...");
                loaded = SettingsManager.createDefault();
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                mapper.setDefaultPrettyPrinter(new SettingsFilePrinter());
                mapper.writeValue(settingsFile, loaded);
                if (settingsFile.exists()) {
                    log.info("Default settings file creation successful.");
                }
            }
        } catch (IOException e) {
            log.error("Failed to resolve settings file, writing default one.", e);
            loaded = SettingsManager.createDefault();
            SettingsManager.writeSettingsToFile(mapper, settingsFile, loaded);
        }
        SettingsManager.settings = loaded;
    }

    private static void writeSettingsToFile(ObjectMapper mapper, File settingsFile, Settings writable) {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            mapper.writeValue(settingsFile, writable);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write settings file!", e);
        }
    }

    private static class SettingsFilePrinter extends DefaultPrettyPrinter {

        private static final String blank = DefaultIndenter.SYS_LF;

        @Override
        public SettingsFilePrinter createInstance() {
            return new SettingsFilePrinter();
        }

        @Override
        public void writeStartObject(JsonGenerator g) throws IOException {
            super.writeStartObject(g);
            g.writeRaw(blank);
        }

        @Override
        public void writeRootValueSeparator(JsonGenerator g) throws IOException {
            super.writeRootValueSeparator(g);
            g.writeRaw(blank);
        }

        @Override
        public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
            g.writeRaw(blank);
            super.writeEndObject(g, nrOfEntries);
        }

    }

}
