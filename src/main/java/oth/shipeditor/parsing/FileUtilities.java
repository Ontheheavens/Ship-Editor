package oth.shipeditor.parsing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.BasicPrettyPrinter;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
@Log4j2
public final class FileUtilities {

    public static final String STARSECTOR_CORE = "starsector-core";

    private static final ObjectMapper mapper;

    public static final String OPEN_COMMAND_CANCELLED_BY_USER = "Open command cancelled by user.";

    @SuppressWarnings("StaticNonFinalField")
    @Getter @Setter
    public static File lastDirectory;

    static {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        mapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        DefaultPrettyPrinter prettyPrinter = new BasicPrettyPrinter().createInstance();
        mapper.setDefaultPrettyPrinter(prettyPrinter);

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    private FileUtilities() {}

    public static void updateActionStates(ViewerLayer currentlySelected) {
        if (!(currentlySelected instanceof ShipLayer layer)) {
            if (currentlySelected != null) {
                LayerPainter painter = currentlySelected.getPainter();
                if (painter == null) {
                    FileLoading.openSpriteAction.setEnabled(true);
                } else {
                    FileLoading.openSpriteAction.setEnabled(painter.getSprite() == null);
                }
            } else {
                FileLoading.openSpriteAction.setEnabled(false);
            }
            FileLoading.openShipDataAction.setEnabled(false);
            return;
        }
        ShipPainter painter = layer.getPainter();
        if (painter == null) {
            FileLoading.openSpriteAction.setEnabled(true);
            FileLoading.openShipDataAction.setEnabled(false);
        } else {
            boolean spriteState = painter.getSprite() == null && layer.getHull() == null;
            boolean hullState = painter.getSprite() != null && layer.getHull() == null;
            FileLoading.openSpriteAction.setEnabled(spriteState);
            FileLoading.openShipDataAction.setEnabled(hullState);
        }
    }

    public static void openPathInDesktop(Path toOpen) {
        FileUtilities.openPathInDesktop(toOpen.toFile());
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void openPathInDesktop(File toOpen) {
        try {
            Desktop.getDesktop().open(toOpen);
        } catch (IOException | IllegalArgumentException ex) {
            log.error("Failed to open {} in Explorer!", toOpen);
            JOptionPane.showMessageDialog(null,
                    "Failed to open file in Explorer, exception thrown at: " + toOpen,
                    "File interaction error!",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static ShipLayer createShipLayerWithSprite(File spriteFile) {
        Sprite sprite = FileLoading.loadSprite(spriteFile);

        var manager = StaticController.getLayerManager();
        var layer = manager.createShipLayer();
        manager.setActiveLayer(layer);
        var viewer = StaticController.getViewer();
        viewer.loadLayer(layer, sprite);

        return layer;
    }

    /**
     * @param targetFile can be either file (CSV table, usually) or a directory.
     * @return map of entries where key is package folder and value is file instance of target in package.
     */
    public static Map<Path, File> getFileFromPackages(Path targetFile) {
        Settings settings = SettingsManager.getSettings();

        List<Path> allModFolders = SettingsManager.getAllModFolders();

        Path coreFolderPath = Path.of(settings.getCoreFolderPath());

        Path coreFilePath = coreFolderPath.resolve(targetFile);

        Map<Path, File> matchingFiles = new LinkedHashMap<>();
        matchingFiles.put(coreFolderPath, coreFilePath.toFile());

        try (Stream<Path> modDirectories = allModFolders.stream()) {
            modDirectories.forEach(modDir -> {
                Path targetFilePath = modDir.resolve(targetFile);
                if (Files.exists(targetFilePath)) {
                    matchingFiles.put(modDir, targetFilePath.toFile());
                }
            });
        }
        return matchingFiles;
    }

    public static ObjectMapper getConfigured() {
        return mapper;
    }

    public static JFileChooser getFileChooser(FileFilter fileFilter) {
        Path coreFolderPath = SettingsManager.getCoreFolderPath();
        JFileChooser fileChooser = new JFileChooser(coreFolderPath.toString());

        File directory = FileUtilities.getLastDirectory();
        if (directory != null) {
            fileChooser.setCurrentDirectory(directory);
        }
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }

    public static File ensureFileExtension(JFileChooser fileChooser, String extension) {
        File selectedFile = fileChooser.getSelectedFile();
        File result;

        String fileName = selectedFile.getName();
        boolean alreadyHasExtension = fileName.endsWith("." + extension);
        if (alreadyHasExtension) {
            result = selectedFile;
        } else {
            result = new File(selectedFile + "." + extension);
        }
        return result;
    }

}
