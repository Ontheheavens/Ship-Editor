package oth.shipeditor.utility.themes;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import lombok.Getter;
import oth.shipeditor.utility.graphics.ColorUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public final class Themes {

    private static final String SPLIT_PANE_BACKGROUND = "SplitPane.background";
    private static final String SPLIT_PANE_DIVIDER_GRIP_COLOR = "SplitPaneDivider.gripColor";
    private static final String SPLIT_PANE_DIVIDER_DRAGGING_COLOR = "SplitPaneDivider.draggingColor";
    private static final String TABBED_PANE_SELECTED_BACKGROUND = "TabbedPane.selectedBackground";
    public static final String TITLE_PANE_BACKGROUND = "TitlePane.background";

    @Getter
    private static Theme activeTheme;

    private Themes() {
    }

    public static Color getIconColor() {
        return activeTheme.getIconColor();
    }

    public static Color getDisabledIconColor() {
        return activeTheme.getDisabledIconColor();
    }

    public static Color getBorderColor() {
        return activeTheme.getBorderColor();
    }

    public static Color getTextColor() {
        return activeTheme.getTextColor();
    }

    public static Color getDisabledTextColor() {
        return activeTheme.getDisabledTextColor();
    }

    public static Color getCorePackageTextColor() {
        return Themes.getReddishFontColor();
    }

    public static Color getPinnedPackageTextColor() {
        return ColorUtilities.getBlendedColor(Themes.getTextColor(), Color.BLUE, 0.75f);
    }

    public static Color getReddishFontColor() {
        return ColorUtilities.getBlendedColor(Themes.getTextColor(), Color.RED, 0.75f);
    }

    public static Color getPanelBackgroundColor() {
        return activeTheme.getPanelBackgroundColor();
    }

    public static Color getPanelHighlightColor() {
        return activeTheme.getPanelHighlightColor();
    }

    public static Color getPanelDarkColor() {
        return activeTheme.getPanelDarkColor();
    }

    public static Color getListBackgroundColor() {
        return activeTheme.getListBackgroundColor();
    }

    public static void setLightTheme() {
        UIManager.put(SPLIT_PANE_BACKGROUND, Color.LIGHT_GRAY);
        UIManager.put(SPLIT_PANE_DIVIDER_GRIP_COLOR, Color.DARK_GRAY);
        UIManager.put(SPLIT_PANE_DIVIDER_DRAGGING_COLOR, Color.BLACK);
        UIManager.put(TABBED_PANE_SELECTED_BACKGROUND, Color.WHITE);
        UIManager.put(TITLE_PANE_BACKGROUND, Color.LIGHT_GRAY);

        FlatIntelliJLaf.setup();

        activeTheme = Theme.LIGHT;
    }

    public static void setDarkTheme() {
        UIManager.put(SPLIT_PANE_BACKGROUND, Color.GRAY);
        UIManager.put(SPLIT_PANE_DIVIDER_GRIP_COLOR, Color.DARK_GRAY);
        UIManager.put(SPLIT_PANE_DIVIDER_DRAGGING_COLOR, Color.BLACK);
        UIManager.put(TABBED_PANE_SELECTED_BACKGROUND, Color.DARK_GRAY);
        UIManager.put(TITLE_PANE_BACKGROUND, Color.GRAY);

        FlatDarkLaf.setup();

        activeTheme = Theme.DARK;
    }

}
