package oth.shipeditor.menubar;

import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.components.viewer.PaintOrderController;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 05.06.2023
 */
class ViewMenu extends JMenu {

    private JMenuItem toggleRotate;

    private JMenuItem toggleCursorGuides;
    private JMenuItem toggleBorders;
    private JMenuItem toggleSpriteCenter;
    private JMenuItem toggleAxes;

    ViewMenu() {
        super("View");
    }

    void initialize() {
        JMenuItem changeBackground = PrimaryMenuBar.createMenuOption("Change background color",
                FluentUiRegularAL.COLOR_BACKGROUND_20,
                event -> {
                    Color chosen = ColorUtilities.showColorChooser();
                    Settings settings = SettingsManager.getSettings();
                    settings.setBackgroundColor(chosen);
                    EventBus.publish(new ViewerBackgroundChanged(chosen));
                });
        this.add(changeBackground);

        JMenuItem displayBackgroundImage = new JCheckBoxMenuItem("Display background image");
        displayBackgroundImage.setIcon(FontIcon.of(FluentUiRegularMZ.TABLE_20, 16, Themes.getIconColor()));
        displayBackgroundImage.setSelected(true);
        displayBackgroundImage.addActionListener(e ->
                PaintOrderController.setShowBackgroundImage(displayBackgroundImage.isSelected()));
        this.add(displayBackgroundImage);

        this.addSeparator();

        JMenuItem resetTransform = PrimaryMenuBar.createMenuOption("Center on selected layer",
                FluentUiRegularMZ.PICTURE_IN_PICTURE_20,
                event ->
                        EventBus.publish(new ViewerTransformsReset())
        );
        this.add(resetTransform);

        this.addSeparator();

        toggleRotate = new JCheckBoxMenuItem("Toggle view rotation");
        toggleRotate.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 16, Themes.getIconColor()));
        toggleRotate.setSelected(true);
        toggleRotate.addActionListener(event ->
                EventBus.publish(new ViewerRotationToggled(toggleRotate.isSelected(), true))
        );
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationToggled checked) {
                toggleRotate.setSelected(checked.isSelected());
                toggleRotate.setEnabled(checked.isEnabled());
            }
        });
        this.add(toggleRotate);

        JMenu guidesSubmenu = this.createGuidesSubmenu();
        this.add(guidesSubmenu);
    }

    private void notifyGuidesToggled() {
        EventBus.publish(new ViewerGuidesToggled(toggleCursorGuides.isSelected(),
                toggleBorders.isSelected(), toggleSpriteCenter.isSelected(),
                toggleAxes.isSelected()));
    }

    private JMenu createGuidesSubmenu() {
        JMenu guidesSubmenu = new JMenu("Toggle guides");
        guidesSubmenu.setIcon(FontIcon.of(BoxiconsRegular.BORDER_INNER, 16, Themes.getIconColor()));

        toggleCursorGuides = new JCheckBoxMenuItem("Enable cursor guides");
        toggleCursorGuides.setSelected(true);
        toggleCursorGuides.addActionListener(e -> this.notifyGuidesToggled());
        guidesSubmenu.add(toggleCursorGuides);

        toggleBorders = new JCheckBoxMenuItem("Enable sprite borders");
        toggleBorders.setSelected(true);
        toggleBorders.addActionListener(e -> this.notifyGuidesToggled());
        guidesSubmenu.add(toggleBorders);

        toggleSpriteCenter = new JCheckBoxMenuItem("Enable sprite center");
        toggleSpriteCenter.setSelected(true);
        toggleSpriteCenter.addActionListener(e -> this.notifyGuidesToggled());
        guidesSubmenu.add(toggleSpriteCenter);

        toggleAxes = new JCheckBoxMenuItem("Enable axis lines");
        toggleAxes.setSelected(true);
        toggleAxes.addActionListener(e -> this.notifyGuidesToggled());
        guidesSubmenu.add(toggleAxes);

        return guidesSubmenu;
    }

}
