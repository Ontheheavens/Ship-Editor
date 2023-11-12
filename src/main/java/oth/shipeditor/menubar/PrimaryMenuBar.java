package oth.shipeditor.menubar;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.Main;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.CursorSnappingToggled;
import oth.shipeditor.communication.events.viewer.control.PointSelectionModeChange;
import oth.shipeditor.communication.events.viewer.control.RotationRoundingToggled;
import oth.shipeditor.components.viewer.control.PointSelectionMode;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.themes.Theme;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

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

        JMenu application = new JMenu("Application");

        application.add(PrimaryMenuBar.createThemeOptions());

        String infoText = "About";
        JMenuItem projectInfo = new JMenuItem(infoText);

        JPanel aboutInfoPanel = new JPanel();
        aboutInfoPanel.setLayout(new BoxLayout(aboutInfoPanel, BoxLayout.PAGE_AXIS));
        aboutInfoPanel.add(new JLabel("Made by: Ontheheavens & Xenoargh"));
        aboutInfoPanel.add(new JLabel("Started: March 2023"));
        String projectVersion = Main.VERSION;
        aboutInfoPanel.add(new JLabel("Current version: " + projectVersion));

        projectInfo.addActionListener(e -> JOptionPane.showMessageDialog(null, aboutInfoPanel,
                infoText, JOptionPane.INFORMATION_MESSAGE));
        application.add(projectInfo);

        this.add(application);
    }

    private static JMenu createThemeOptions() {
        JMenu themeMenu = new JMenu("Theme");
        themeMenu.setIcon(FontIcon.of(FluentUiRegularAL.DARK_THEME_24, 16, Themes.getIconColor()));

        Settings settings = SettingsManager.getSettings();
        String themeHint = "Will take effect after restart";

        var themes = Theme.values();

        ButtonGroup buttonGroup = new ButtonGroup();

        for (Theme theme : themes) {
            JMenuItem setTheme = new JRadioButtonMenuItem(theme.getDisplayedName());
            setTheme.addActionListener(e -> settings.setTheme(theme));
            setTheme.setToolTipText(themeHint);

            buttonGroup.add(setTheme);

            Theme settingsTheme = settings.getTheme();
            if (settingsTheme == theme) {
                setTheme.setSelected(true);
            }

            themeMenu.add(setTheme);
        }

        return themeMenu;
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
        undo.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_UNDO_20, 16, Themes.getIconColor()));
        undo.setDisabledIcon(FontIcon.of(FluentUiRegularAL.ARROW_UNDO_20, 16, Themes.getDisabledIconColor()));
        KeyStroke keyStrokeToUndo = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK);
        undo.setAccelerator(keyStrokeToUndo);
        editMenu.add(undo);

        JMenuItem redo = new JMenuItem("Redo");
        redo.setAction(UndoOverseer.getRedoAction());
        redo.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_REDO_20, 16, Themes.getIconColor()));
        redo.setDisabledIcon(FontIcon.of(FluentUiRegularAL.ARROW_REDO_20, 16, Themes.getDisabledIconColor()));
        KeyStroke keyStrokeToRedo = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        redo.setAccelerator(keyStrokeToRedo);
        editMenu.add(redo);

        editMenu.addSeparator();

        JMenuItem pointSelectionMode = PrimaryMenuBar.createPointSelectionModeOptions();
        editMenu.add(pointSelectionMode);

        toggleCursorSnap = new JCheckBoxMenuItem("Toggle cursor snapping");
        toggleCursorSnap.setIcon(FontIcon.of(FluentUiRegularAL.GROUP_20, 16, Themes.getIconColor()));
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
        toggleRotationRounding.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 16, Themes.getIconColor()));
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
        newSubmenu.setIcon(FontIcon.of(FluentUiRegularMZ.TARGET_20, 16, Themes.getIconColor()));

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
        LayersMenu layersMenu = new LayersMenu();
        layersMenu.initialize();
        return layersMenu;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static JMenuItem createMenuOption(String text, Ikon icon, ActionListener action) {
        JMenuItem newOption = new JMenuItem(text);
        newOption.setIcon(FontIcon.of(icon, 16, Themes.getIconColor()));
        newOption.addActionListener(action);
        return newOption;
    }

}
