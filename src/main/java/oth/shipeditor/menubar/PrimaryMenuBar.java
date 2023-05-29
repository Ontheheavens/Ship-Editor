package oth.shipeditor.menubar;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.Window;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public class PrimaryMenuBar extends JMenuBar {

    private final Window parent;

    @Getter
    private FileMenu fileMenu;

    @Getter
    private JMenuItem toggleRotate;

    public PrimaryMenuBar(Window parent) {
        this.parent = parent;
        this.add(createFileMenu());
        this.add(createViewMenu());
    }

    private JMenu createFileMenu() {
        fileMenu = new FileMenu();
        fileMenu.initialize();
        return fileMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("View");

        JMenuItem changeBackground = this.createMenuOption("Change background color",  FluentUiRegularAL.COLOR_BACKGROUND_20,
                l -> SwingUtilities.invokeLater(() ->  {
                    Color chosen = JColorChooser.showDialog(parent, "Choose Background", Color.GRAY);
                    EventBus.publish(new ViewerBackgroundChanged(chosen));
                }));
        viewMenu.add(changeBackground);

        JMenuItem resetTransform = this.createMenuOption("Reset view transforms",
                FluentUiRegularMZ.PICTURE_IN_PICTURE_20,
                l -> SwingUtilities.invokeLater(() ->
                        EventBus.publish(new ViewerTransformsReset())));
        viewMenu.add(resetTransform);

        toggleRotate = new JCheckBoxMenuItem("Toggle view rotation");
        toggleRotate.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 16));
        toggleRotate.addActionListener(l -> SwingUtilities.invokeLater(() ->
                EventBus.publish(new ViewerRotationToggled(toggleRotate.isSelected(), true))));
        EventBus.subscribe(ViewerRotationToggled.class, event -> {
            toggleRotate.setSelected(event.isSelected());
            toggleRotate.setEnabled(event.isEnabled());
        });
        viewMenu.add(toggleRotate);

        return viewMenu;
    }

    private JMenuItem createMenuOption(String text, Ikon icon, ActionListener action) {
        JMenuItem newOption = new JMenuItem(text);
        newOption.setIcon(FontIcon.of(icon, 16));
        newOption.addActionListener(action);
        return newOption;
    }



}
