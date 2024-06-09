package oth.shipeditor.menubar;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.Main;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.themes.Theme;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 09.06.2024
 */
public class ApplicationMenu extends JMenu {

    ApplicationMenu() {
        super("Application");
    }

    public void initialize() {
        this.add(ApplicationMenu.createThemeOptions());

        String infoText = "About";
        JMenuItem projectInfo = new JMenuItem(infoText);

        JPanel aboutInfoPanel = new JPanel();
        aboutInfoPanel.setLayout(new BoxLayout(aboutInfoPanel, BoxLayout.PAGE_AXIS));
        aboutInfoPanel.add(new JLabel("Authors: Ontheheavens & Xenoargh"));
        aboutInfoPanel.add(new JLabel("Contributors: Nathan67003"));
        aboutInfoPanel.add(new JLabel("Started: March 2023"));
        String projectVersion = Main.VERSION;
        aboutInfoPanel.add(new JLabel("Current version: " + projectVersion));

        projectInfo.addActionListener(e -> JOptionPane.showMessageDialog(null, aboutInfoPanel,
                infoText, JOptionPane.INFORMATION_MESSAGE));
        this.add(projectInfo);
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

}
