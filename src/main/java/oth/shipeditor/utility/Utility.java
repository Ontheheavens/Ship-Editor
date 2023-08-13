package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

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

    @SuppressWarnings("WeakerAccess")
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

    public static String getWithLinebreaks(String ... lines) {
        StringBuilder builder = new StringBuilder("<html>" );
        Stream<String> stringStream = Arrays.stream(lines);
        stringStream.forEachOrdered(line -> {
            builder.append(line);
            builder.append("<br>");
        });
        builder.append("</html>");
        return builder.toString();
    }

    public static String getPointPositionText(Point2D location) {
            return location.getX() + ", " + location.getY();
    }

    public static Point2D getPointCoordinatesForDisplay(Point2D pointPosition) {
        Point2D result = pointPosition;
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        if (activeLayer == null) {
            return result;
        }
        LayerPainter layerPainter = activeLayer.getPainter();
        if (layerPainter == null || layerPainter.isUninitialized()) {
            return result;
        }
        double positionX = pointPosition.getX();
        double positionY = pointPosition.getY();
        switch (StaticController.getCoordsMode()) {
            case WORLD -> {
                AffineTransform transform = layerPainter.getRotationTransform();
                result = transform.transform(result, null);
            }
            case SPRITE_CENTER -> {
                Point2D center = layerPainter.getSpriteCenter();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(positionX - centerX, positionY - centerY);

            }
            case SHIPCENTER_ANCHOR -> {
                if (!(layerPainter instanceof ShipPainter checkedPainter)) break;
                Point2D center = checkedPainter.getCenterAnchor();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(positionX - centerX, (-positionY + centerY));
            }
            // This case uses different coordinate system alignment to be consistent with game files.
            // Otherwise, user might be confused as shown point coordinates won't match with those in file.
            case SHIP_CENTER -> {
                if (!(layerPainter instanceof ShipPainter checkedPainter)) break;
                BaseWorldPoint shipCenter = checkedPainter.getShipCenter();
                Point2D center = shipCenter.getPosition();
                double centerX = center.getX();
                double centerY = center.getY();
                result = new Point2D.Double(-(positionY - centerY), -(positionX - centerX));
            }
        }
        result = Utility.roundPointCoordinates(result, 3);
        return result;
    }

    public static double clampAngleWithRounding(double radians) {
        double rotationDegrees = Math.toDegrees(radians);
        double clampedDegrees = (360 - rotationDegrees) % 360;
        return Utility.round(clampedDegrees, 5);
    }

    public static double flipAngle(double degrees) {
        double flipped = -degrees;
        return (flipped + 360) % 360;
    }

    public static String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase(Locale.ROOT) + input.substring(1);
    }

}
