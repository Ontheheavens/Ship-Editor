package oth.shipeditor.menubar;

import lombok.Getter;
import lombok.Setter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
                    Color chosen = ViewMenu.showColorChooser();
                    Settings settings = SettingsManager.getSettings();
                    settings.setBackgroundColor(chosen);
                    EventBus.publish(new ViewerBackgroundChanged(chosen));
                });
        this.add(changeBackground);
        JMenuItem resetTransform = PrimaryMenuBar.createMenuOption("Center on selected layer",
                FluentUiRegularMZ.PICTURE_IN_PICTURE_20,
                event ->
                        EventBus.publish(new ViewerTransformsReset())
        );
        this.add(resetTransform);
        toggleRotate = new JCheckBoxMenuItem("Toggle view rotation");
        toggleRotate.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 16));
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

    /**
     * This method is employed to get rid of some chooser panels and tweak cancel behaviour.
     * @return Color instance that was selected in chooser dialogue.
     */
    private static Color showColorChooser() {
        Color initial = Color.GRAY;
        JColorChooser chooser = new JColorChooser(initial);
        AbstractColorChooserPanel[] chooserPanels = chooser.getChooserPanels();
        for (AbstractColorChooserPanel chooserPanel : chooserPanels) {
            Class<? extends AbstractColorChooserPanel> panelClass = chooserPanel.getClass();
            String clsName = panelClass.getName();
            if ("javax.swing.colorchooser.DefaultSwatchChooserPanel".equals(clsName)) {
                chooser.removeChooserPanel(chooserPanel);
            }
        }
        for (AbstractColorChooserPanel ccPanel : chooserPanels) {
            ccPanel.setColorTransparencySelectionEnabled(false);
        }
        final class ColorListener implements ActionListener {
            private final JColorChooser chooser;
            @Getter @Setter
            private Color color;
            private ColorListener(JColorChooser colorChooser) {
                this.chooser = colorChooser;
            }
            public void actionPerformed(ActionEvent e) {
                color = chooser.getColor();
            }
        }
        ColorListener colorTracker = new ColorListener(chooser);
        class DisposeChooserOnClose extends ComponentAdapter {
            public void componentHidden(ComponentEvent e) {
                Window window = (Window) e.getComponent();
                window.dispose();
            }
        }
        JDialog dialog = JColorChooser.createDialog(null, "Choose Background",
                true, chooser, colorTracker, e -> colorTracker.setColor(initial));
        dialog.addComponentListener(new DisposeChooserOnClose());
        dialog.setVisible(true);
        return colorTracker.getColor();
    }

}
