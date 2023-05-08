package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.data.ShipData;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
        openSubmenu = new JMenu("Open");
        openSubmenu.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16));

        JMenuItem openSprite = new JMenuItem("Open sprite");
        JFileChooser spriteChooser = new JFileChooser();
        FileNameExtensionFilter spriteFilter = new FileNameExtensionFilter(
                "PNG Images", "png");
        spriteChooser.setFileFilter(spriteFilter);

        openSprite.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16));
        openSprite.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            PrimaryWindow parentFrame = PrimaryWindow.getInstance();
            int returnVal = spriteChooser.showOpenDialog(parentFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = spriteChooser.getSelectedFile();
                try {
                    BufferedImage sprite = ImageIO.read(file);

                    if (parentFrame.getShipView() != null) {
                        parentFrame.getShipView().loadShipSprite(sprite);
                    } else {
                        parentFrame.loadShipView(sprite);
                    }

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                log.info("Opening: " + file.getName() + ".");
            } else {
                log.info("Open command cancelled by user.");
            }
        }));
        openSubmenu.add(openSprite);

        JMenuItem openShipData = new JMenuItem("Open ship file");
        JFileChooser shipDataChooser = new JFileChooser();
        FileNameExtensionFilter shipDatafilter = new FileNameExtensionFilter(
                "JSON ship files", "ship");
        shipDataChooser.setFileFilter(shipDatafilter);

        openShipData.setIcon(FontIcon.of(FluentUiRegularAL.CLIPBOARD_TEXT_20, 16));
        openShipData.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            PrimaryWindow parentFrame = PrimaryWindow.getInstance();
            int returnVal = shipDataChooser.showOpenDialog(parentFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = shipDataChooser.getSelectedFile();
                ShipData data = new ShipData(file);

                if (parentFrame.getShipData() != null) {
                    parentFrame.setShipData(data);
                } else {
                    parentFrame.loadEditingPanes(data);
                }

                log.info("Opening: " + file.getName() + ".");
            } else {
                log.info("Open command cancelled by user.");
            }
        }));
        openSubmenu.add(openShipData);

        this.add(openSubmenu);
    }

}
