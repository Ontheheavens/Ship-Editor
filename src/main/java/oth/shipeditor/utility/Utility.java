package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
        return Utility.setAlphaComposite(g, 1.0f);
    }

    public static Composite setAlphaComposite(Graphics2D g, double alpha) {
        int rule = AlphaComposite.SRC_OVER;
        Composite old = g.getComposite();
        Composite opacity = AlphaComposite.getInstance(rule, (float) alpha) ;
        g.setComposite(opacity);
        return old;
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

    public static Point2D roundPointCoordinates(Point2D point, int decimalPlaces) {
        double roundedX = Utility.round(point.getX(), decimalPlaces);
        double roundedY = Utility.round(point.getY(), decimalPlaces);
        return new Point2D.Double(roundedX, roundedY);
    }

    public static double round(double value, int decimalPlaces) {
        if (decimalPlaces < 0) throw new IllegalArgumentException("Decimal places cannot be negative.");
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

}
