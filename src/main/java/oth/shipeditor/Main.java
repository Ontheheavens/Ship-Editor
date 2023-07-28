package oth.shipeditor;

import com.formdev.flatlaf.FlatIntelliJLaf;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.LastLayerSelectQueued;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.Initializations;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.undo.UndoOverseer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
@Log4j2
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // First four method calls are initialization block; the order of said calls is important.
            Main.configureLaf();
            PrimaryWindow window = PrimaryWindow.create();
            Initializations.updateStateFromSettings(window);
            Initializations.loadGameData(window);

            Main.testFilesNew(window);

            window.showGUI();
        });
    }

    private static void configureLaf() {
        FlatIntelliJLaf.setup();

        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);

        UIManager.put("SplitPane.dividerSize", 8);
        UIManager.put("SplitPane.oneTouchButtonSize", 10);
        UIManager.put("SplitPane.background", Color.LIGHT_GRAY);
        UIManager.put("SplitPaneDivider.gripColor", Color.DARK_GRAY);
        UIManager.put("SplitPaneDivider.draggingColor", Color.BLACK);

        UIManager.put("TitlePane.background", Color.LIGHT_GRAY);
        UIManager.put("TitlePane.useWindowDecorations", true);

        UIManager.put("Tree.paintLines", true);
        UIManager.put("Tree.showDefaultIcons", true);
        UIManager.put("TitlePane.showIcon", true);
        UIManager.put("TitlePane.showIconInDialogs", true);
        UIManager.put("FileChooser.readOnly", true);

        UIManager.put(Initializations.FILE_CHOOSER_SHORTCUTS_FILES_FUNCTION, (Function<File[], File[]>) files -> {
            ArrayList<File> list = new ArrayList<>( Arrays.asList( files ) );
            list.removeIf(next -> Initializations.SHELL_FOLDER_0_X_12.equals(next.getPath()));
            return list.toArray(new File[0]);
        } );
    }

    private static void testFilesNew(PrimaryWindow window) {
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, ShipCSVEntry> allShips = gameData.getAllShipEntries();

        ShipCSVEntry crigEntry = allShips.get("crig");
        crigEntry.loadLayerFromEntry();

        ShipCSVEntry legionEntry = allShips.get("legion");
        Map<String, Skin> legionSkins = legionEntry.getSkins();
        Skin legionXIV = legionSkins.get("legion_xiv.skin");
        legionEntry.setActiveSkin(legionXIV);
        legionEntry.loadLayerFromEntry();

        ShipViewable shipView = window.getShipView();
        LayerManager layerManager = shipView.getLayerManager();
        ViewerLayer activeLayer = layerManager.getActiveLayer();
        LayerPainter painter = activeLayer.getPainter();
        painter.updateAnchorOffset(new Point2D.Double(-400, 0));
        UndoOverseer.finishAllEdits();
        shipView.centerViewpoint();
    }

    @SuppressWarnings("unused")
    private static void testFilesOld(PrimaryWindow window) {
        String legionSprite = "legion_xiv.png";
        String crigSprite = "salvage_rig.png";
        String legionHull = "legion.ship";
        String crigHull = "constructionrig.ship";
        Main.loadShip(window, crigSprite, crigHull);
        Main.loadShip(window, legionSprite, legionHull);
        ShipViewable shipView = window.getShipView();
        LayerManager layerManager = shipView.getLayerManager();
        ViewerLayer activeLayer = layerManager.getActiveLayer();
        LayerPainter painter = activeLayer.getPainter();
        painter.updateAnchorOffset(new Point2D.Double(-400, 0));
        UndoOverseer.finishAllEdits();
        shipView.centerViewpoint();
    }

    private static void loadShip(PrimaryWindow window, String spriteFilePath, String hullFilePath) {
        EventBus.publish(new ShipLayerCreationQueued());
        EventBus.publish(new LastLayerSelectQueued());
        Class<? extends PrimaryWindow> windowClass = window.getClass();
        ClassLoader classLoader = windowClass.getClassLoader();
        URL spritePath = Objects.requireNonNull(classLoader.getResource(spriteFilePath));
        File spriteFile;
        try {
            spriteFile = new File(spritePath.toURI());
            BufferedImage sprite = FileLoading.loadSprite(spriteFile);
            EventBus.publish(new SpriteOpened(sprite, spriteFile.getName()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        URL dataPath = Objects.requireNonNull(classLoader.getResource(hullFilePath));
        try {
            URI url = dataPath.toURI();
            File hullFile = new File(url);
            Hull hull = FileLoading.loadHullFile(hullFile);
            EventBus.publish(new HullFileOpened(hull, hullFile.getName()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
