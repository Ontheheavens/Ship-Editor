package oth.shipeditor;

import com.formdev.flatlaf.FlatIntelliJLaf;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.logging.StandardOutputRedirector;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.Initializations;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
@Log4j2
public final class Main {

    public static final String TREE_PAINT_LINES = "Tree.paintLines";

    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // These method calls are initialization block; the order of calls is important.
            StandardOutputRedirector.redirectStandardStreams();
            Main.configureLaf();
            PrimaryWindow window = PrimaryWindow.create();
            Initializations.updateStateFromSettings(window);

            window.showGUI();

            Settings settings = SettingsManager.getSettings();

            if (settings.isLoadDataAtStart()) {
                CompletableFuture<List<Runnable>> gameDataLoading = FileLoading.loadGameData();
                if (settings.isLoadTestFiles()) {
                    CompletableFuture<Void> testing = gameDataLoading.thenRun(Main::testFiles);
                }
            }
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

        UIManager.put(TREE_PAINT_LINES, true);
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

    private static void testFiles() {
        SwingUtilities.invokeLater(() -> {
            var gameData = SettingsManager.getGameData();
            var allShips = gameData.getAllShipEntries();

            ShipCSVEntry crigEntry = allShips.get("crig");
            crigEntry.loadLayerFromEntry();

            ShipCSVEntry legionEntry = allShips.get("legion");
            var legionSkins = legionEntry.getSkins();
            SkinSpecFile legionXIV = legionSkins.get("legion_xiv.skin");
            legionEntry.setActiveSkinSpecFile(legionXIV);
            legionEntry.loadLayerFromEntry();

            ViewerLayer activeLayer = StaticController.getActiveLayer();
            LayerPainter painter = activeLayer.getPainter();
            painter.updateAnchorOffset(new Point2D.Double(-400, 0));
            UndoOverseer.finishAllEdits();
            EventBus.publish(new ViewerTransformsReset());

            if (activeLayer instanceof ShipLayer shipLayer) {
                Variant legionXIVVariant = GameDataRepository.getVariantByID("legion_xiv_Elite");
                ShipPainter shipPainter = shipLayer.getPainter();
                shipPainter.selectVariant(legionXIVVariant);
            }
        });
    }

}
