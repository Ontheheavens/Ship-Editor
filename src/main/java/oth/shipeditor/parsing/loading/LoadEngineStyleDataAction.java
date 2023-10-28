package oth.shipeditor.parsing.loading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.EngineStylesLoaded;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 19.08.2023
 */
@Log4j2
public class LoadEngineStyleDataAction extends DataLoadingAction {

    @Override
    public Runnable perform() {
        Path targetFile = Paths.get("data", StringConstants.CONFIG, "engine_styles.json");

        Map<Path, File> engineStyleFiles = FileUtilities.getFileFromPackages(targetFile);

        Map<String, EngineStyle> collectedEngineStyles = new LinkedHashMap<>();
        for (Map.Entry<Path, File> entry : engineStyleFiles.entrySet()) {
            File styleFile = entry.getValue();
            log.info("Engine style data file found in mod directory: {}", entry.getKey());
            Map<String, EngineStyle> stylesFromFile = LoadEngineStyleDataAction.loadEngineStyleFile(styleFile);
            for (EngineStyle style : stylesFromFile.values()) {
                style.setContainingPackage(entry.getKey());
            }
            collectedEngineStyles.putAll(stylesFromFile);
        }

        return () -> {
            GameDataRepository gameData = SettingsManager.getGameData();
            gameData.setAllEngineStyles(collectedEngineStyles);
            EventBus.publish(new EngineStylesLoaded(collectedEngineStyles));
        };
    }

    private static Map<String, EngineStyle> loadEngineStyleFile(File styleFile) {
        ObjectMapper mapper = FileUtilities.getConfigured();
        Map<String, EngineStyle> engineStyles;
        log.info("Fetching engine style data at: {}..", styleFile.toPath());
        MapType mapType = null;
        try {
            TypeFactory typeFactory = mapper.getTypeFactory();
            mapType = typeFactory.constructMapType(HashMap.class, String.class, EngineStyle.class);
            engineStyles = mapper.readValue(styleFile, mapType);
        } catch (IOException e) {
            log.error("Engine styles file loading failed, retrying with correction: {}", styleFile.getName());
            engineStyles = FileLoading.parseCorrectableJSON(styleFile, mapType);
        }

        if (engineStyles == null) {
            log.error("Engine styles file loading failed conclusively: {}", styleFile.getName());
            JOptionPane.showMessageDialog(null,
                    "Engine styles file loading failed, exception thrown at: " + styleFile,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Engine styles file loading failed conclusively!");
        }

        for (Map.Entry<String, EngineStyle> entry : engineStyles.entrySet()) {
            String engineStyleID = entry.getKey();
            EngineStyle engineStyle = entry.getValue();
            engineStyle.setEngineStyleID(engineStyleID);
            engineStyle.setFilePath(styleFile.toPath());
        }

        return engineStyles;
    }
}
