package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.Window;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
public class FileMenu extends JMenu {

    private JMenu openSubmenu;

    public FileMenu() {
        super("File");
    }

    public void initialize() {
        openSubmenu = this.createOpenSubmenu();
        this.add(openSubmenu);
    }

    public JMenu createOpenSubmenu() {
        JMenu newSubmenu = new JMenu("Open");
        newSubmenu.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16));

        JMenuItem openSprite = new JMenuItem("Open sprite");
        openSprite.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16));
        openSprite.addActionListener(l ->
                SwingUtilities.invokeLater(Files.createOpenSpriteAction()));
        newSubmenu.add(openSprite);

        JMenuItem openShipData = new JMenuItem("Open ship file");
        openShipData.setIcon(FontIcon.of(FluentUiRegularAL.CLIPBOARD_TEXT_20, 16));
        openShipData.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            JFileChooser shipDataChooser = new JFileChooser();
            FileNameExtensionFilter shipDataFilter = new FileNameExtensionFilter(
                    "JSON ship files", "ship");
            shipDataChooser.setFileFilter(shipDataFilter);
            Window parentFrame = Window.getFrame();
            int returnVal = shipDataChooser.showOpenDialog(parentFrame);
            Files.tryOpenShipData(returnVal, shipDataChooser, parentFrame);
        }));
        newSubmenu.add(openShipData);

        return newSubmenu;
    }

}
