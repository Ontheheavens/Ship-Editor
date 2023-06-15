package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.SelectedLayerRemovalQueued;
import oth.shipeditor.undo.UndoOverseer;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public final class PrimaryMenuBar extends JMenuBar {

    public PrimaryMenuBar() {
        this.add(PrimaryMenuBar.createFileMenu());
        this.add(PrimaryMenuBar.createEditMenu());
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

    private static JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> UndoOverseer.undoEdit());
        editMenu.add(undo);
        return editMenu;
    }

    private static JMenu createLayersMenu() {
        JMenu layersMenu = new JMenu("Layers");

        JMenuItem createLayer = new JMenuItem("Create new layer");
        createLayer.setIcon(FontIcon.of(FluentUiRegularMZ.ROCKET_16, 16));
        createLayer.addActionListener(event -> SwingUtilities.invokeLater(
                        () -> EventBus.publish(new LayerCreationQueued())
                )
        );
        layersMenu.add(createLayer);

        JMenuItem removeLayer = new JMenuItem("Remove selected layer");
        removeLayer.addActionListener(event -> SwingUtilities.invokeLater(
                        () -> EventBus.publish(new SelectedLayerRemovalQueued())
                )
        );
        layersMenu.add(removeLayer);

        return layersMenu;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static JMenuItem createMenuOption(String text, Ikon icon, ActionListener action) {
        JMenuItem newOption = new JMenuItem(text);
        newOption.setIcon(FontIcon.of(icon, 16));
        newOption.addActionListener(action);
        return newOption;
    }

}
