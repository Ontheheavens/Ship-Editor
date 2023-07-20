package oth.shipeditor.utility.graphics;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 20.07.2023
 */
@SuppressWarnings("unused")
public final class ColorUtilities {

    private ColorUtilities() {
    }

    public static Color darken(Color color, double factor) {
        double darkenFactor = factor;
        if (darkenFactor < 0) {
            darkenFactor = 0;
        } else if (darkenFactor > 1) {
            darkenFactor = 1;
        }
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = (int) (red * (1 - darkenFactor));
        green = (int) (green * (1 - darkenFactor));
        blue = (int) (blue * (1 - darkenFactor));

        red = Math.max(0, red);
        green = Math.max(0, green);
        blue = Math.max(0, blue);

        return new Color(red, green, blue);
    }

    public static Color setFullAlpha(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }

    public static Color getBlendedColor(Color first, Color second, double ratio) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Color blending fail: both colors must not be null!");
        }

        if (ratio < 0 || ratio > 1) {
            throw new IllegalArgumentException("Color blending fail: blend ratio should be between 0 and 1!");
        }
        int red = (int) (first.getRed() * (1 - ratio) + second.getRed() * ratio);
        int green = (int) (first.getGreen() * (1 - ratio) + second.getGreen() * ratio);
        int blue = (int) (first.getBlue() * (1 - ratio) + second.getBlue() * ratio);
        int alpha = (int) (first.getAlpha() * (1 - ratio) + second.getAlpha() * ratio);

        return new Color(red, green, blue, alpha);
    }

    public static float getOpacityFromAlpha(int alpha) {
        return alpha / 255.0f; // Convert alpha [0, 255] to opacity [0.0, 1.0].
    }

}
