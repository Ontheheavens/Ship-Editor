package oth.shipeditor.menubar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.HullStylesLoaded;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.LastLayerSelectQueued;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerCreationQueued;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.parsing.loading.*;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.HullStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
@Log4j2
public final class FileUtilities {

    public static final String STARSECTOR_CORE = "starsector-core";

    @Getter
    private static final Action loadShipDataAction = new LoadShipDataAction();

    @Getter
    private static final Action loadHullmodDataAction = new LoadHullmodDataAction();

    @Getter
    private static final Action openSpriteAction = new OpenSpriteAction();

    @Getter
    private static final Action openShipDataAction = new OpenHullAction();

    @Getter
    private static final Action loadHullAsLayerAction = new LoadHullAsLayer();

    private FileUtilities() {}

    public static void updateActionStates(ViewerLayer currentlySelected) {
        if (!(currentlySelected instanceof ShipLayer layer)) {
            openSpriteAction.setEnabled(false);
            openShipDataAction.setEnabled(false);
            return;
        }
        boolean spriteState = layer.getSprite() == null && layer.getShipData() == null;
        boolean hullState = layer.getSprite() != null && layer.getShipData() == null;
        openSpriteAction.setEnabled(spriteState);
        openShipDataAction.setEnabled(hullState);
    }

    public static void openPathInDesktop(Path toOpen) {
        FileUtilities.openPathInDesktop(toOpen.toFile());
    }

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

    public static void createLayerWithSprite(File spriteFile) {
        EventBus.publish(new ShipLayerCreationQueued());
        EventBus.publish(new LastLayerSelectQueued());
        BufferedImage sprite = FileLoading.loadSprite(spriteFile);
        EventBus.publish(new SpriteOpened(sprite, spriteFile.getName()));
    }

    private static class OpenHullAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileLoading.openHullAndDo(e1 -> {
                    JFileChooser shipDataChooser = (JFileChooser) e1.getSource();
                    File file = shipDataChooser.getSelectedFile();
                    Hull hull = FileLoading.loadHullFile(file);
                    EventBus.publish(new HullFileOpened(hull, file.getName()));
            });
        }
    }

    public static void loadHullStyles() {
        Settings settings = SettingsManager.getSettings();

        Path targetFile = Paths.get("data", "config", "hull_styles.json");

        List<Path> allModFolders = SettingsManager.getAllModFolders();

        Path coreFolderPath = Path.of(settings.getCoreFolderPath());
        Path coreFilePath = coreFolderPath.resolve(targetFile);

        Map<Path, File> hullStyleFiles = new LinkedHashMap<>();
        hullStyleFiles.put(coreFolderPath, coreFilePath.toFile());

        try (Stream<Path> modDirectories = allModFolders.stream()) {
            modDirectories.forEach(modDir -> {
                Path targetFilePath = modDir.resolve(targetFile);
                if (Files.exists(targetFilePath)) {
                    hullStyleFiles.put(modDir, targetFilePath.toFile());
                }
            });
        }

        Map<String, HullStyle> collectedHullStyles = new LinkedHashMap<>();
        for (Map.Entry<Path, File> entry : hullStyleFiles.entrySet()) {
            File styleFile = entry.getValue();

            Map<String, HullStyle> stylesFromFile = FileUtilities.loadHullStyleFile(styleFile);
            for (HullStyle style : stylesFromFile.values()) {
                style.setContainingPackage(entry.getKey());
            }
            collectedHullStyles.putAll(stylesFromFile);
        }
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setAllHullStyles(collectedHullStyles);
        EventBus.publish(new HullStylesLoaded(collectedHullStyles));
    }

    private static Map<String, HullStyle> loadHullStyleFile(File styleFile) {
        ObjectMapper mapper = FileLoading.getConfigured();
        Map<String, HullStyle> hullStyles = null;
        try {
            TypeFactory typeFactory = mapper.getTypeFactory();
            hullStyles = mapper.readValue(styleFile,
                    typeFactory.constructMapType(HashMap.class, String.class, HullStyle.class));

            for (Map.Entry<String, HullStyle> entry : hullStyles.entrySet()) {
                String hullStyleID = entry.getKey();
                HullStyle hullStyle = entry.getValue();
                hullStyle.setHullStyleID(hullStyleID);
                hullStyle.setFilePath(styleFile.toPath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return hullStyles;
    }

}
