package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;

import javax.swing.*;

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

        SettingsMenu settings = new SettingsMenu();
        settings.initialize();
        this.add(settings);

        ApplicationMenu application = new ApplicationMenu();
        application.initialize();
        this.add(application);
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
        EditMenu editMenu = new EditMenu();
        editMenu.initialize();
        return editMenu;
    }

    private static JMenu createLayersMenu() {
        LayersMenu layersMenu = new LayersMenu();
        layersMenu.initialize();
        return layersMenu;
    }

}
