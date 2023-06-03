package oth.shipeditor.menubar;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
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
public final class PrimaryMenuBar extends JMenuBar {

    @Getter
    private FileMenu fileMenu;

    @Getter
    private JMenuItem toggleRotate;

    public PrimaryMenuBar() {
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

        JMenuItem changeBackground = this.createMenuOption("Change background color",
                FluentUiRegularAL.COLOR_BACKGROUND_20,
                event -> {
                    Color chosen = JColorChooser.showDialog(null, "Choose Background", Color.GRAY);
                    EventBus.publish(new ViewerBackgroundChanged(chosen));
                });
        viewMenu.add(changeBackground);

        JMenuItem resetTransform = this.createMenuOption("Reset view transforms",
                FluentUiRegularMZ.PICTURE_IN_PICTURE_20,
                event ->
                        EventBus.publish(new ViewerTransformsReset())
        );
        viewMenu.add(resetTransform);

        toggleRotate = new JCheckBoxMenuItem("Toggle view rotation");
        toggleRotate.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 16));
        toggleRotate.addActionListener(event ->
                EventBus.publish(new ViewerRotationToggled(toggleRotate.isSelected(), true))
        );
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationToggled checked) {
                toggleRotate.setSelected(checked.isSelected());
                toggleRotate.setEnabled(checked.isEnabled());
            }
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
