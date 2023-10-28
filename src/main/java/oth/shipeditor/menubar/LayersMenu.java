package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerRemovalQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.ViewerLayerRemovalConfirmed;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreationQueued;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 28.10.2023
 */
@Log4j2
class LayersMenu extends JMenu {

    private JMenuItem removeLayer;

    private JMenuItem createHullData;

    LayersMenu() {
        super("Layers");
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    void initialize() {
        JMenuItem createLayer = new JMenuItem("Create new layer");
        createLayer.setIcon(FontIcon.of(BoxiconsRegular.LAYER_PLUS, 16));
        createLayer.addActionListener(event -> {
            Object[] options = {"Ship Layer", "Weapon Layer"};
            int result = JOptionPane.showOptionDialog(null,
                    "Select new layer type:",
                    "Create New Layer",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (result == 0) {
                EventBus.publish(new ShipLayerCreationQueued());
            } else {
                EventBus.publish(new WeaponLayerCreationQueued());
            }
        });
        this.add(createLayer);

        removeLayer = new JMenuItem("Remove selected layer");
        removeLayer.setIcon(FontIcon.of(BoxiconsRegular.LAYER_MINUS, 16));
        removeLayer.addActionListener(event -> EventBus.publish(new ActiveLayerRemovalQueued()));

        EventBus.subscribe(event -> {
            LayerManager layerManager = StaticController.getLayerManager();
            if (layerManager != null) {
                if (event instanceof ViewerLayerRemovalConfirmed checked && layerManager.isEmpty()) {
                    removeLayer.setEnabled(false);
                } else if (event instanceof LayerWasSelected && !layerManager.isEmpty()) {
                    removeLayer.setEnabled(true);
                }
            }
        });

        this.add(removeLayer);
        this.addSeparator();

        JMenuItem printViewer = LayersMenu.getViewerPrintOption();
        this.add(printViewer);
        this.addSeparator();

        createHullData = new JMenuItem("Create new ship data");
        createHullData.setIcon(FontIcon.of(BoxiconsRegular.DETAIL, 16));
        createHullData.addActionListener(event -> {
            HullSpecFile created = new HullSpecFile();
            EventBus.publish(new HullFileOpened(created, null));
        });
        this.add(createHullData);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static JMenuItem getViewerPrintOption() {
        JMenuItem printViewer = new JMenuItem("Print viewer to image");
        printViewer.setIcon(FontIcon.of(BoxiconsRegular.IMAGE_ADD, 16));

        printViewer.addActionListener(event -> {
            FileNameExtensionFilter pngFilter = new FileNameExtensionFilter(
                    "PNG Image", "png");
            var chooser = FileUtilities.getFileChooser(pngFilter);
            chooser.setDialogTitle("Print viewer content to image file");

            int returnVal = chooser.showSaveDialog(null);
            FileUtilities.setLastDirectory(chooser.getCurrentDirectory());

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String extension = ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0];
                File result = FileUtilities.ensureFileExtension(chooser, extension);
                log.info("Commencing viewer printing: {}", result);

                var viewer = StaticController.getViewer();
                int width = viewer.getWidth();
                int height = viewer.getHeight();
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();

                viewer.print(g2d);
                g2d.dispose();
                try {
                    javax.imageio.ImageIO.write(image , extension, result);
                } catch (IOException e) {
                    log.error("Viewer printing failed: {}", result.getName());
                    JOptionPane.showMessageDialog(null,
                            "Viewer printing failed, exception thrown at: " + result,
                            StringValues.FILE_SAVING_ERROR,
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        return printViewer;
    }

}
