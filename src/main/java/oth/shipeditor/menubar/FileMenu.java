package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;

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

        JMenuItem loadHullAsLayer = new JMenuItem(FileUtilities.getLoadHullAsLayerAction());
        loadHullAsLayer.setIcon(FontIcon.of(BoxiconsRegular.LAYER, 16));
        loadHullAsLayer.setText("Load ship file as layer");
        this.add(loadHullAsLayer);
    }

    private static JMenu createOpenSubmenu() {
        JMenu newSubmenu = new JMenu("Open");
        newSubmenu.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16));

        JMenuItem openSprite = new JMenuItem(FileUtilities.getOpenSpriteAction());
        openSprite.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16));
        openSprite.setText("Open sprite to layer");
        newSubmenu.add(openSprite);

        JMenuItem openShipData = new JMenuItem(FileUtilities.getOpenShipDataAction());
        openShipData.setIcon(FontIcon.of(FluentUiRegularAL.CLIPBOARD_TEXT_20, 16));
        openShipData.setText("Open ship file to layer");
        newSubmenu.add(openShipData);

        return newSubmenu;
    }

}
