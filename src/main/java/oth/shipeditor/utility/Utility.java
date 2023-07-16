package oth.shipeditor.utility;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
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

    public static void drawBorderedLine(Graphics2D canvas, Point2D start, Point2D finish, Color inner) {
        Utility.drawBorderedLine(canvas, start, finish, inner, Color.BLACK, 2.0f, 3.0f);
    }

    public static Polygon createHexagon(Point2D point, int radius) {
        int x = (int) point.getX(), y = (int) point.getY();

        int[] xPoints = new int[6];
        int[] yPoints = new int[6];

        // Calculate the coordinates of the six points of the hexagon.
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            xPoints[i] = x + (int) (radius * Math.cos(angle));
            yPoints[i] = y + (int) (radius * Math.sin(angle));
        }
        return new Polygon(xPoints, yPoints, 6);
    }

    @SuppressWarnings("unused")
    public static Shape getScaledShape(Shape input, Point2D center,
                                       AffineTransform delegateWTS,
                                       AffineTransform worldToScreen, float scale) {
        AffineTransform scaleTX = new AffineTransform();
        scaleTX.translate(center.getX(), center.getY());
        scaleTX.scale(scale, scale);
        scaleTX.translate(-center.getX(), -center.getY());
        delegateWTS.setTransform(worldToScreen);
        delegateWTS.concatenate(scaleTX);
        return delegateWTS.createTransformedShape(input);
    }

    @SuppressWarnings({"SameParameterValue", "MethodWithTooManyParameters"})
    private static void drawBorderedLine(Graphics2D canvas, Point2D start, Point2D finish,
                                         Color innerColor, Color outerColor, float innerWidth, float outerWidth) {
        Stroke originalStroke = canvas.getStroke();
        Color originalColor = canvas.getColor();
        canvas.setColor(outerColor);
        canvas.setStroke(new BasicStroke(outerWidth));
        canvas.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());
        canvas.setColor(innerColor);
        canvas.setStroke(new BasicStroke(innerWidth));
        canvas.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());
        canvas.setStroke(originalStroke);
        canvas.setColor(originalColor);
    }

    public static Point2D correctAdjustedCursor(Point2D adjustedCursor, AffineTransform screenToWorld) {
        Point2D wP = screenToWorld.transform(adjustedCursor, null);
        double roundedX = Math.round(wP.getX() * 2) / 2.0;
        double roundedY = Math.round(wP.getY() * 2) / 2.0;
        return new Point2D.Double(roundedX, roundedY);
    }

    public static RectangularShape createCircle(Point2D position, float radius) {
        return new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
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

}
