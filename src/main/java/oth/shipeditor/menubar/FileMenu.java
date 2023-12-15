package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.parsing.JsonProcessor;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */

@Log4j2
class FileMenu extends JMenu {

    FileMenu() {
        super("File");
    }

    void initialize() {
        JMenu openSubmenu = FileMenu.createOpenSubmenu();
        this.add(openSubmenu);

        JMenuItem loadHullAsLayer = new JMenuItem(FileLoading.getLoadHullAsLayer());
        loadHullAsLayer.setIcon(FontIcon.of(BoxiconsRegular.LAYER, 16, Themes.getIconColor()));
        loadHullAsLayer.setText("Load ship file as layer");
        this.add(loadHullAsLayer);


        JMenuItem loadSpriteAsHull = new JMenuItem(FileLoading.getLoadSpriteAsHull());
        loadSpriteAsHull.setIcon(FontIcon.of(BoxiconsRegular.LAYER, 16, Themes.getIconColor()));
        loadSpriteAsHull.setText("Load sprite as new hull");
        this.add(loadSpriteAsHull);

        JMenuItem jsonCorrector = FileMenu.getJSONCorrector();
        jsonCorrector.setIcon(FontIcon.of(FluentUiRegularMZ.TEXT_GRAMMAR_OPTIONS_20, 16, Themes.getIconColor()));
        this.add(jsonCorrector);
    }

    private static JMenu createOpenSubmenu() {
        JMenu newSubmenu = new JMenu("Open");
        newSubmenu.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16, Themes.getIconColor()));

        JMenuItem openSprite = new JMenuItem(FileLoading.getOpenSprite());
        openSprite.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16, Themes.getIconColor()));
        openSprite.setText("Open sprite to layer");
        newSubmenu.add(openSprite);

        JMenuItem openShipData = new JMenuItem(FileLoading.getOpenShip());
        openShipData.setIcon(FontIcon.of(FluentUiRegularAL.CLIPBOARD_TEXT_20, 16, Themes.getIconColor()));
        openShipData.setText("Open ship file to layer");
        newSubmenu.add(openShipData);

        return newSubmenu;
    }

    private static JMenuItem getJSONCorrector() {
        JMenuItem jsonCorrector = new JMenuItem("Correct non-conforming JSON");
        jsonCorrector.setToolTipText("Fixes semantically incorrect JSON, then saves it to the same location");
        jsonCorrector.addActionListener(e -> {
            JFileChooser fileChooser = FileUtilities.getFileChooser();

            File directory = FileUtilities.getLastGeneralDirectory();
            if (directory != null) {
                fileChooser.setCurrentDirectory(directory);
            }

            int returnVal = fileChooser.showOpenDialog(null);
            File currentDirectory = fileChooser.getCurrentDirectory();
            FileUtilities.setLastGeneralDirectory(currentDirectory);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                String result = JsonProcessor.straightenMalformed(file);

                String fixedFileName = Utility.getFilenameWithoutExtension(file.getName()) + "_corrected.json";

                String path = currentDirectory.getPath();
                String targetFilePath = path + "\\" + fixedFileName;
                try (PrintWriter out = new PrintWriter(targetFilePath, StandardCharsets.UTF_8)) {
                    out.println(result);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return jsonCorrector;
    }

}
