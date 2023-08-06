package oth.shipeditor.menubar;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.LastLayerSelectQueued;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.parsing.loading.*;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.utility.graphics.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Log4j2
public final class FileUtilities {

    public static final String STARSECTOR_CORE = "starsector-core";

    @Getter
    private static final Action loadShipDataAction = new LoadShipDataAction();

    @Getter
    private static final Action loadHullmodDataAction = new LoadHullmodDataAction();

    @Getter
    private static final Action loadHullStyleDataAction = new LoadHullStyleDataAction();

    @Getter
    private static final Action loadShipSystemDataAction = new LoadShipSystemDataAction();

    @Getter
    private static final Action loadWingDataAction = new LoadWingDataAction();

    @Getter
    private static final Action loadWeaponDataAction = new LoadWeaponsDataAction();

    @Getter
    private static final Action openSpriteAction = new OpenSpriteAction();

    @Getter
    private static final Action openShipDataAction = new OpenHullAction();

    @Getter
    private static final Action loadHullAsLayerAction = new LoadHullAsLayer();

    @SuppressWarnings("StaticCollection")
    @Getter
    private static final List<Action> loadDataActions = FileUtilities.initLoadActions();

    private FileUtilities() {}

    private static List<Action> initLoadActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(loadShipDataAction);
        actions.add(loadHullmodDataAction);
        actions.add(loadHullStyleDataAction);
        actions.add(loadShipSystemDataAction);
        actions.add(loadWingDataAction);
        actions.add(loadWeaponDataAction);
        return actions;
    }

    public static void updateActionStates(ViewerLayer currentlySelected) {
        if (!(currentlySelected instanceof ShipLayer layer)) {
            openSpriteAction.setEnabled(currentlySelected != null && currentlySelected.getSprite() == null);
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

    public static void createShipLayerWithSprite(File spriteFile) {
        EventBus.publish(new ShipLayerCreationQueued());
        EventBus.publish(new LastLayerSelectQueued());
        Sprite sprite = FileLoading.loadSprite(spriteFile);
        EventBus.publish(new SpriteOpened(sprite));
    }

    private static class OpenHullAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileLoading.openHullAndDo(event -> {
                    JFileChooser shipDataChooser = (JFileChooser) event.getSource();
                    File file = shipDataChooser.getSelectedFile();
                    HullSpecFile hullSpecFile = FileLoading.loadHullFile(file);
                    EventBus.publish(new HullFileOpened(hullSpecFile, file.getName()));
            });
        }
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

}
