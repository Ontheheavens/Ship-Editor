package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
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
    }

    private static JMenu createOpenSubmenu() {
        JMenu newSubmenu = new JMenu("Open");
        newSubmenu.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16));

        JMenuItem openSprite = new JMenuItem("Open sprite");
        openSprite.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16));
        openSprite.addActionListener(event -> SwingUtilities.invokeLater(FileUtilities.createOpenSpriteAction())
        );
        newSubmenu.add(openSprite);

        JMenuItem openShipData = new JMenuItem("Open ship file");
        openShipData.setIcon(FontIcon.of(FluentUiRegularAL.CLIPBOARD_TEXT_20, 16));
        openShipData.addActionListener(event -> SwingUtilities.invokeLater(FileUtilities.createOpenHullFileAction()));
        newSubmenu.add(openShipData);

        return newSubmenu;
    }

}
