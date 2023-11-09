package oth.shipeditor.utility.themes;

import oth.shipeditor.utility.graphics.ColorUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public final class Themes {

    private Themes() {
    }

    public static Color getIconColor() {
        return UIManager.getColor("Menu.icon.arrowColor");
    }

    public static Color getDisabledIconColor() {
        return UIManager.getColor("Menu.icon.disabledArrowColor");
    }

    public static Color getBorderColor() {
        return UIManager.getColor("Component.borderColor");
    }

    public static Color getTextColor() {
        return UIManager.getColor("Label.foreground");
    }

    public static Color getDisabledTextColor() {
        return UIManager.getColor("Label.disabledForeground");
    }

    public static Color getCorePackageTextColor() {
        return Themes.getReddishFontColor();
    }

    public static Color getPinnedPackageTextColor() {
        return ColorUtilities.getBlendedColor(Themes.getTextColor(), Color.BLUE, 0.75f);
    }

    public static Color getTitleBackgroundColor() {
        return UIManager.getColor("TitlePane.background");
    }

    public static Color getReddishFontColor() {
        return ColorUtilities.getBlendedColor(Themes.getTextColor(), Color.RED, 0.75f);
    }

    public static Color getPanelBackgroundColor() {
        return UIManager.getColor("Panel.background");
    }

    public static Color getPanelHighlightColor() {
        return UIManager.getColor("TextArea.background");
    }

    public static Color getPanelDarkColor() {
        return UIManager.getColor("TextArea.disabledBackground");
    }

    public static Color getListBackgroundColor() {
        return UIManager.getColor("List.background");
    }

}
