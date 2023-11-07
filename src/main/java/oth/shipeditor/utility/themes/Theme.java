package oth.shipeditor.utility.themes;

import lombok.Getter;
import oth.shipeditor.utility.graphics.ColorUtilities;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
@SuppressWarnings("ParameterHidesMemberVariable")
@Getter
public enum Theme {

    LIGHT(Color.BLACK,
            Color.GRAY,
            Color.BLACK,
            Color.GRAY,
            Color.LIGHT_GRAY,
            Color.LIGHT_GRAY,
            Color.GRAY,
            Color.LIGHT_GRAY,
            Color.WHITE),

    DARK(Color.LIGHT_GRAY,
            Color.GRAY,
            Color.LIGHT_GRAY,
            Color.GRAY,
            Color.GRAY,
            Color.DARK_GRAY,
            Color.GRAY,
            ColorUtilities.getBlendedColor(Color.DARK_GRAY, Color.BLACK, 0.25f),
            Color.DARK_GRAY);

    private final Color textColor;

    private final Color disabledTextColor;

    private final Color iconColor;

    private final Color disabledIconColor;

    private final Color borderColor;

    private final Color panelBackgroundColor;

    private final Color panelHighlightColor;

    private final Color panelDarkColor;

    private final Color listBackgroundColor;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    Theme(Color textColor, Color disabledTextColor, Color iconColor, Color disabledIconColor,
          Color borderColor, Color panelBackgroundColor, Color panelHighlightColor, Color panelDarkColor, Color listBackgroundColor) {
        this.textColor = textColor;
        this.disabledTextColor = disabledTextColor;
        this.iconColor = iconColor;
        this.disabledIconColor = disabledIconColor;
        this.borderColor = borderColor;
        this.panelBackgroundColor = panelBackgroundColor;
        this.panelHighlightColor = panelHighlightColor;
        this.panelDarkColor = panelDarkColor;
        this.listBackgroundColor = listBackgroundColor;
    }
}
