package oth.shipeditor;

import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public class PrimaryMenuBar {

    private final PrimaryWindow parent;
    @Getter
    private final JMenuBar menuBar;

    public PrimaryMenuBar(PrimaryWindow parent) {
        this.parent = parent;
        menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createViewMenu());
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem openOption = fileMenu.add(new JMenuItem("Open"));
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PNG Images", "png");
        chooser.setFileFilter(filter);

        openOption.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16));
        openOption.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            int returnVal = chooser.showOpenDialog(parent.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    BufferedImage sprite = ImageIO.read(file);
                    parent.getShipView().setShipSprite(sprite);
                    parent.getStatusPanel().setDimensionsLabelString(sprite);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                log.info("Opening: " + file.getName() + ".");
            } else {
                log.info("Open command cancelled by user.");
            }
        }));

        return fileMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("View");
        Viewer shipView = parent.getShipView().getViewer();

        JMenuItem backgroundColorOption = viewMenu.add(new JMenuItem("Change background color"));
        backgroundColorOption.setIcon(FontIcon.of(FluentUiRegularAL.COLOR_BACKGROUND_20, 16));
        backgroundColorOption.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            Color chosen = JColorChooser.showDialog(parent.getMainFrame(), "Choose Background", Color.GRAY);
            shipView.setBackground(chosen);
            shipView.repaint();
        }));

        JMenuItem resetTransform = viewMenu.add(new JMenuItem("Reset view transforms"));
        resetTransform.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_HOOK_UP_LEFT_20, 16));
        resetTransform.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            shipView.resetTransform();
            parent.getShipView().getControls().setZoomLevel(1);
            parent.getShipView().centerViewpoint();
        }));

        return viewMenu;
    }

}
