package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.CursorSnappingToggled;
import oth.shipeditor.communication.events.viewer.control.PointSelectionModeChange;
import oth.shipeditor.communication.events.viewer.control.RotationRoundingToggled;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerRemovalQueued;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreationQueued;
import oth.shipeditor.components.viewer.control.PointSelectionMode;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public final class PrimaryMenuBar extends JMenuBar {

    private JCheckBoxMenuItem toggleCursorSnap;

    private JCheckBoxMenuItem toggleRotationRounding;

    public PrimaryMenuBar() {
        this.add(PrimaryMenuBar.createFileMenu());
        this.add(this.createEditMenu());
        this.add(PrimaryMenuBar.createViewMenu());
        this.add(PrimaryMenuBar.createLayersMenu());
    }

    private static JMenu createFileMenu() {
        FileMenu fileMenu = new FileMenu();
        fileMenu.initialize();
        return fileMenu;
    }

    private static JMenu createViewMenu() {
        ViewMenu viewMenu = new ViewMenu();
        viewMenu.initialize();
        return viewMenu;
    }

    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undo = new JMenuItem("Undo");
        undo.setAction(UndoOverseer.getUndoAction());
        undo.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_UNDO_20, 16));
        undo.setDisabledIcon(FontIcon.of(FluentUiRegularAL.ARROW_UNDO_20, 16, Color.GRAY));
        KeyStroke keyStrokeToUndo = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK);
        undo.setAccelerator(keyStrokeToUndo);
        editMenu.add(undo);

        JMenuItem redo = new JMenuItem("Redo");
        redo.setAction(UndoOverseer.getRedoAction());
        redo.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_REDO_20, 16));
        redo.setDisabledIcon(FontIcon.of(FluentUiRegularAL.ARROW_REDO_20, 16, Color.GRAY));
        KeyStroke keyStrokeToRedo = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        redo.setAccelerator(keyStrokeToRedo);
        editMenu.add(redo);

        editMenu.addSeparator();

        JMenuItem pointSelectionMode = PrimaryMenuBar.createPointSelectionModeOptions();
        editMenu.add(pointSelectionMode);

        toggleCursorSnap = new JCheckBoxMenuItem("Toggle cursor snapping");
        toggleCursorSnap.setIcon(FontIcon.of(FluentUiRegularAL.GROUP_20, 16));
        toggleCursorSnap.setSelected(true);
        toggleCursorSnap.addActionListener(event ->
                EventBus.publish(new CursorSnappingToggled(toggleCursorSnap.isSelected()))
        );
        EventBus.subscribe(event -> {
            if (event instanceof CursorSnappingToggled checked) {
                toggleCursorSnap.setSelected(checked.toggled());
            }
        });
        editMenu.add(toggleCursorSnap);

        toggleRotationRounding = new JCheckBoxMenuItem("Toggle rotation rounding");
        toggleRotationRounding.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 16));
        toggleRotationRounding.setSelected(true);
        toggleRotationRounding.addActionListener(event ->
                EventBus.publish(new RotationRoundingToggled(toggleRotationRounding.isSelected()))
        );
        EventBus.subscribe(event -> {
            if (event instanceof RotationRoundingToggled checked) {
                toggleRotationRounding.setSelected(checked.toggled());
            }
        });
        editMenu.add(toggleRotationRounding);

        return editMenu;
    }

    private static JMenu createPointSelectionModeOptions() {
        JMenu newSubmenu = new JMenu("Point selection mode");
        newSubmenu.setIcon(FontIcon.of(FluentUiRegularMZ.TARGET_20, 16));

        JMenuItem selectHovered = new JRadioButtonMenuItem("Select clicked");
        selectHovered.addActionListener(e ->
                EventBus.publish(new PointSelectionModeChange(PointSelectionMode.STRICT)));
        newSubmenu.add(selectHovered);

        JMenuItem selectClosest = new JRadioButtonMenuItem("Select closest");
        selectClosest.addActionListener(e ->
                EventBus.publish(new PointSelectionModeChange(PointSelectionMode.CLOSEST)));
        newSubmenu.add(selectClosest);
        selectClosest.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(selectHovered);
        buttonGroup.add(selectClosest);

        EventBus.subscribe(event -> {
            if (event instanceof PointSelectionModeChange checked) {
                if (checked.newMode() == PointSelectionMode.STRICT && !selectHovered.isSelected()) {
                    selectHovered.setSelected(true);
                } else if (checked.newMode() == PointSelectionMode.CLOSEST && !selectClosest.isSelected()) {
                    selectClosest.setSelected(true);
                }
            }
        });

        return newSubmenu;
    }

    private static JMenu createLayersMenu() {
        JMenu layersMenu = new JMenu("Layers");

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
        layersMenu.add(createLayer);

        JMenuItem removeLayer = new JMenuItem("Remove selected layer");
        removeLayer.setIcon(FontIcon.of(BoxiconsRegular.LAYER_MINUS, 16));
        removeLayer.addActionListener(event -> EventBus.publish(new ActiveLayerRemovalQueued()));
        layersMenu.add(removeLayer);

        JMenuItem printViewer = PrimaryMenuBar.getViewerPrintOption();
        layersMenu.add(printViewer);

        return layersMenu;
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

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static JMenuItem createMenuOption(String text, Ikon icon, ActionListener action) {
        JMenuItem newOption = new JMenuItem(text);
        newOption.setIcon(FontIcon.of(icon, 16));
        newOption.addActionListener(action);
        return newOption;
    }

}
