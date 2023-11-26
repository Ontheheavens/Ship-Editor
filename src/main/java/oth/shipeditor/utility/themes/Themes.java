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

    public static Color getDarkerBackgroundColor() {
        return ColorUtilities.getBlendedColor(Themes.getPanelDarkColor(), Color.BLACK, 0.15f);
    }

    public static Color getTabBackgroundColor() {
        return ColorUtilities.getBlendedColor(Themes.getTabColor(), Color.BLACK, 0.05f);
    }

    public static Color getBrighterSelectionColor() {
        return UIManager.getColor("Button.default.hoverBackground");
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
        return ColorUtilities.getBlendedColor(Themes.getPanelBackgroundColor(), Color.BLACK, 0.15f);
    }

    public static Color getListBackgroundColor() {
        return UIManager.getColor("List.background");
    }

    public static Color getListDisabledColor() {
        return UIManager.getColor("ComboBox.disabledBackground");
    }

    private static Color getTabColor() {
        return UIManager.getColor("TabbedPane.background");
    }

    public static void setupColors() {
        UIManager.put("SplitPane.background", Themes.getPanelDarkColor());

        String gripColorID = "SplitPaneDivider.gripColor";
        Color gripColor = UIManager.getColor(gripColorID);
        Color darkerGripColor = ColorUtilities.getBlendedColor(gripColor,
                Color.BLACK, 0.5f);

        String dividerDraggingColorID = "SplitPaneDivider.draggingColor";
        Color dividerDraggingColor = UIManager.getColor(dividerDraggingColorID);
        Color darkDividerDraggingColor = ColorUtilities.getBlendedColor(dividerDraggingColor,
                Color.BLACK, 0.25f);

        UIManager.put(gripColorID, darkerGripColor);
        UIManager.put(dividerDraggingColorID, darkDividerDraggingColor);

        Color selectedTabColor = ColorUtilities.getBlendedColor(Themes.getTabColor(),
                Color.WHITE, 0.05f);
        UIManager.put("TabbedPane.selectedBackground", selectedTabColor);
    }

}
