package oth.shipeditor.utility.graphics;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.*;

/**
 * @author Ontheheavens
 * @since 20.07.2023
 */
@SuppressWarnings("unused")
public final class ColorUtilities {

    private ColorUtilities() {
    }

    public static Color darken(Color color, double factor) {
        double darkenFactor = ColorUtilities.clampValue(factor, 0, 1);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = ColorUtilities.adjustComponent(red, darkenFactor);
        green = ColorUtilities.adjustComponent(green, darkenFactor);
        blue = ColorUtilities.adjustComponent(blue, darkenFactor);

        return new Color(red, green, blue);
    }

    public static Color lighten(Color color, double factor) {
        double lightenFactor = ColorUtilities.clampValue(factor, 0, 1);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = ColorUtilities.adjustComponent(red, 1 + lightenFactor);
        green = ColorUtilities.adjustComponent(green, 1 + lightenFactor);
        blue = ColorUtilities.adjustComponent(blue, 1 + lightenFactor);

        return new Color(red, green, blue);
    }

    private static int adjustComponent(int component, double factor) {
        double result = (component * factor);
        return (int) ColorUtilities.clampValue(result, 0, 255);
    }

    @SuppressWarnings("SameParameterValue")
    private static double clampValue(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static Color setFullAlpha(Color color) {
        return ColorUtilities.setColorAlpha(color, 255);
    }


    /**
     * @param alpha The alpha value to set (0 to 255, where 0 is fully transparent and 255 is fully opaque).
     */
    @SuppressWarnings("WeakerAccess")
    public static Color setColorAlpha(Color color, int alpha) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int maxAlpha = Math.max(0, Math.min(alpha, 255));
        return new Color(red, green, blue, maxAlpha);
    }

    @SuppressWarnings("WeakerAccess")
    public static Color setHalfAlpha(Color color) {
        int alpha = color.getAlpha();
        alpha = alpha / 2;
        alpha = Math.max(0, Math.min(255, alpha));
        return ColorUtilities.setColorAlpha(color, alpha);
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

    @SuppressWarnings("FloatingPointEquality")
    public static int getHueFromRGB(int red, int green, int blue) {
        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);
        if (min == max) {
            return 0;
        }
        float hue;
        if (max == red) {
            hue = (green - blue) / (max - min);
        } else if (max == green) {
            hue = 2.0f + (blue - red) / (max - min);
        } else {
            hue = 4.0f + (red - green) / (max - min);
        }
        hue = hue * 60;
        if (hue < 0) hue = hue + 360;
        return Math.round(hue);
    }

    public static String getColorBreakdown(Color color) {
        int colorRed = color.getRed();
        int colorGreen = color.getGreen();
        int colorBlue = color.getBlue();
        int colorAlpha = color.getAlpha();
        return "<html>" +
                "<p>" + "Red: " + colorRed + "</p>" +
                "<p>" + "Green: " + colorGreen + "</p>" +
                "<p>" + "Blue: " + colorBlue + "</p>" +
                "<p>" + "Alpha: " + colorAlpha + "</p>" +
                "</html>";
    }

    /**
     * @param colorString expected to be comma-separated RGBA values, e.g. "255,255,255,255".
     */
    public static Color convertStringToColor(String colorString) {
        String[] components = colorString.split(",");
        int r = Integer.parseInt(components[0].trim());
        int g = Integer.parseInt(components[1].trim());
        int b = Integer.parseInt(components[2].trim());
        int a = Integer.parseInt(components[3].trim());

        return new Color(r, g, b, a);
    }

    public static String convertColorToString(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        return String.format("%d,%d,%d,%d", red, green, blue, alpha);
    }

    public static Color showColorChooser() {
        Color initial = Color.GRAY;
        return ColorUtilities.showColorChooser(initial);
    }

    /**
     * This method is employed to get rid of some chooser panels and tweak cancel behaviour.
     * @return Color instance that was selected in chooser dialogue.
     */
    public static Color showColorChooser(Color initial) {

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
            @Getter
            @Setter
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

    public static BufferedImage clearToTransparent(BufferedImage image) {
        Color color = Color.GRAY;
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        Image transparent = ColorUtilities.makeColorTransparent(image, color);

        return ColorUtilities.imageToBufferedImage(transparent);
    }

    private static Image makeColorTransparent(BufferedImage image, Color color) {
        ImageFilter filter = new RGBImageFilter() {
            final int markerRGB = color.getRGB() | 0xFF000000;

            public int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    return 0x00FFFFFF & rgb;
                } else {
                    return rgb;
                }
            }
        };

        ImageProducer imageProducer = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(imageProducer);
    }

    private static BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return bufferedImage;
    }


}
