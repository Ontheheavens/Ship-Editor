package oth.shipeditor.menubar;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 09.06.2024
 */
public class SettingsMenu extends JMenu {

    SettingsMenu() {
        super("Settings");
    }

    public void initialize() {
        Settings settings = SettingsManager.getSettings();

        JMenuItem autoLoadData = new JCheckBoxMenuItem("Auto-load data at start");
        autoLoadData.setSelected(SettingsManager.isDataAutoloadEnabled());
        autoLoadData.setIcon(FontIcon.of(FluentUiRegularAL.DOCUMENT_AUTOSAVE_24, 16, Themes.getIconColor()));
        autoLoadData.addActionListener(event ->
                settings.setLoadDataAtStart(autoLoadData.isSelected())
        );
        this.add(autoLoadData);

        JMenuItem toggleFileErrorPopups = new JCheckBoxMenuItem("Enable file error pop-ups");
        toggleFileErrorPopups.setSelected(SettingsManager.areFileErrorPopupsEnabled());
        toggleFileErrorPopups.setIcon(FontIcon.of(FluentUiRegularAL.DOCUMENT_ERROR_20, 16, Themes.getIconColor()));
        toggleFileErrorPopups.addActionListener(event ->
                settings.setShowLoadingErrors(toggleFileErrorPopups.isSelected())
        );
        this.add(toggleFileErrorPopups);

        this.addSeparator();

        JMenuItem toggleLoadSpritesAnywhere = new JCheckBoxMenuItem("Load sprites from anywhere");
        toggleLoadSpritesAnywhere.setSelected(SettingsManager.isLoadingSpritesFromAnywhereEnabled());
        toggleLoadSpritesAnywhere.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16, Themes.getIconColor()));
        toggleLoadSpritesAnywhere.addActionListener(event ->
                settings.setLoadSpritesFromAnywhere(toggleLoadSpritesAnywhere.isSelected())
        );
        this.add(toggleLoadSpritesAnywhere);
    }

}
