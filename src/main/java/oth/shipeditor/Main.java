package oth.shipeditor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.logging.StandardOutputRedirector;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.Initializations;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.themes.Theme;

import javax.swing.*;
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

    @Getter
    private static PrimaryWindow window;

    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // These method calls are initialization block; the order of calls is important.
            StandardOutputRedirector.redirectStandardStreams();
            Initializations.initializeSettingsFile();
            Main.configureLaf();
            window = PrimaryWindow.create();
            Initializations.updateStateFromSettings(window);

            window.showGUI();

            Settings settings = SettingsManager.getSettings();

            if (settings.isLoadDataAtStart()) {
                CompletableFuture<List<Runnable>> gameDataLoading = FileLoading.loadGameData();
            }
        });
    }

    private static void configureLaf() {
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
        UIManager.put("SplitPane.dividerSize", 8);
        UIManager.put("SplitPane.oneTouchButtonSize", 10);
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

        Settings settings = SettingsManager.getSettings();
        Theme settingsTheme = settings.getTheme();
        Runnable setterMethod = settingsTheme.getSetterMethod();
        setterMethod.run();
    }

}
