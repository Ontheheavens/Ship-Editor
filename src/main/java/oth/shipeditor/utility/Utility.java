package oth.shipeditor.utility;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
@Log4j2
public final class Utility {

    /**
     * Private constructor prevents instantiation of utility class.
     */
    private Utility() {}

    public static Composite setFullAlpha(Graphics2D g) {
        int rule = AlphaComposite.SRC_OVER;
        Composite old = g.getComposite();
        Composite opacity = AlphaComposite.getInstance(rule, 1.0f) ;
        g.setComposite(opacity);
        return old;
    }

    @SuppressWarnings("unused")
    public static AffineTransform getScreenToWorldRotation(AffineTransform worldToScreen, Point2D positionWorld) {
        Point2D positionScreen = worldToScreen.transform(positionWorld, null);

        double screenX = positionScreen.getX(), screenY = positionScreen.getY();

        // Extracting the rotation component from the worldToScreen transform.
        // I don't understand even half of it - bless my new machine overlords.
        double[] matrix = new double[6];
        worldToScreen.getMatrix(matrix);
        double scaleX = Math.sqrt(matrix[0] * matrix[0] + matrix[1] * matrix[1]);
        double rotationAngle = -Math.atan2(matrix[1] / scaleX, matrix[0] / scaleX);

        AffineTransform rotationTransform = new AffineTransform();
        // New AffineTransform by default is focused  on the 0,0 in screen coordinates.
        // We have to center it on our point, do the rotation, then translate back.
        rotationTransform.translate(screenX, screenY);
        rotationTransform.rotate(-rotationAngle);
        rotationTransform.translate(-screenX, -screenY);

        return rotationTransform;
    }

    public static float getOpacityFromAlpha(int alpha) {
        return alpha / 255.0f; // Convert alpha [0, 255] to opacity [0.0, 1.0].
    }

    public static Font getOrbitron(int size) {
        return new Font("Orbitron", Font.BOLD, size);
    }

    public static Point2D correctAdjustedCursor(Point2D adjustedCursor, AffineTransform screenToWorld) {
        Point2D wP = screenToWorld.transform(adjustedCursor, null);
        double roundedX = Math.round(wP.getX() * 2) / 2.0;
        double roundedY = Math.round(wP.getY() * 2) / 2.0;
        return new Point2D.Double(roundedX, roundedY);
    }

    public static JLabel getIconLabelWithBorder(Icon icon) {
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setOpaque(true);
        imageLabel.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
        imageLabel.setBackground(Color.LIGHT_GRAY);
        return imageLabel;
    }

    public static ActionListener scheduleTask(int waitTime, ActionListener taskBeforeStart, ActionListener taskWhenDone) {
        return e -> {
            taskBeforeStart.actionPerformed(e);
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws InterruptedException {
                    Thread.sleep(waitTime);
                    return null;
                }
                @Override
                protected void done() {
                    taskWhenDone.actionPerformed(e);
                }
            };
            worker.execute();
        };
    }

    public static Pair<JSlider, JLabel> createOpacityWidget(ChangeListener change,
                                  BusEventListener eventListener) {
        JSlider opacitySlider = new JSlider(SwingConstants.HORIZONTAL,
                0, 100, 100);
        opacitySlider.setAlignmentX(0.0f);
        opacitySlider.setEnabled(false);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(change);
        EventBus.subscribe(eventListener);
        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0%"));
        labelTable.put(50, new JLabel("50%"));
        labelTable.put(100, new JLabel("100%"));
        opacitySlider.setLabelTable(labelTable);
        opacitySlider.setMajorTickSpacing(50);
        opacitySlider.setMinorTickSpacing(10);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        JLabel opacityLabel = new JLabel();
        opacityLabel.setAlignmentX(0.0f);
        return new Pair<>(opacitySlider, opacityLabel);
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

}
