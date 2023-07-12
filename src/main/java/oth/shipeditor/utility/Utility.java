package oth.shipeditor.utility;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

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

    @SuppressWarnings({"SameParameterValue", "MethodWithTooManyParameters"})
    private static void drawBorderedLine(Graphics2D canvas, Point2D start, Point2D finish,
                                         Color innerColor, Color outerColor, float innerWidth, float outerWidth) {
        Stroke originalStroke = canvas.getStroke();
        canvas.setColor(outerColor);
        canvas.setStroke(new BasicStroke(outerWidth));
        canvas.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());
        canvas.setColor(innerColor);
        canvas.setStroke(new BasicStroke(innerWidth));
        canvas.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());
        canvas.setStroke(originalStroke);
    }

    public static Point2D correctAdjustedCursor(Point2D adjustedCursor, AffineTransform screenToWorld) {
        Point2D wP = screenToWorld.transform(adjustedCursor, null);
        double roundedX = Math.round(wP.getX() * 2) / 2.0;
        double roundedY = Math.round(wP.getY() * 2) / 2.0;
        return new Point2D.Double(roundedX, roundedY);
    }

    public static JSeparator clone(JSeparator original) {
        JSeparator copy = new JSeparator(original.getOrientation());
        copy.setPreferredSize(original.getPreferredSize());
        return copy;
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

}
