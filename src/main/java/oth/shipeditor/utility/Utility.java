package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
@SuppressWarnings("ClassWithTooManyMethods")
@Log4j2
public final class Utility {

    @SuppressWarnings("RegExpSimplifiable")
    public static final Pattern SPLIT_BY_COMMA = Pattern.compile(",[ ]*");
    private static final Pattern FILE_EXTENSION = Pattern.compile("[.][^.]+$");

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

    public static Font getDefaultFont() {
        MenuContainer label = new JLabel();
        return label.getFont();
    }

    public static Point2D getSpriteCenterDifferenceToAnchor(RenderedImage image) {
        return new Point2D.Double((image.getWidth() / 2.0f), (image.getHeight() / 2.0f));
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
            if (line == null || line.isEmpty()) return;
            builder.append(line);
            builder.append("<br>");
        });
        String builderUnfinished = builder.toString();
        if ("<html>".equals(builderUnfinished)) return "";
        builder.append("</html>");
        return builder.toString();
    }

    public static String getTooltipForSprite(Sprite sprite) {
        String spriteName = "Filename: " + sprite.getFilename();
        BufferedImage image = sprite.getImage();
        String width = "Width: " + image.getWidth();
        String height = "Height: " + image.getHeight();
        return Utility.getWithLinebreaks(spriteName, width, height);
    }

    public static String getPointPositionText(Point2D location) {
            return location.getX() + ", " + location.getY();
    }

    public static Point2D getPointCoordinatesForDisplay(Point2D pointPosition) {
        CoordsDisplayMode coordsMode = StaticController.getCoordsMode();
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        if (activeLayer == null) {
            return pointPosition;
        }
        LayerPainter layerPainter = activeLayer.getPainter();
        if (layerPainter == null || layerPainter.isUninitialized()) {
            return pointPosition;
        }
        return Utility.getPointCoordinatesForDisplay(pointPosition, layerPainter, coordsMode);
    }

    public static Point2D getPointCoordinatesForDisplay(Point2D pointPosition, LayerPainter layerPainter,
                                                        CoordsDisplayMode mode) {
        Point2D result = pointPosition;

        double positionX = pointPosition.getX();
        double positionY = pointPosition.getY();
        switch (mode) {
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

    public static double transformAngle(double raw) {
        double transformed = raw % 360;
        if (transformed < 0) {
            transformed += 360;
        }

        transformed = (360 - transformed) % 360;
        return transformed - 90;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void setSpriteFromPath(String pathInPackage, Consumer<Sprite> setter, Path packageFolderPath) {
        if (pathInPackage != null && !pathInPackage.isEmpty()) {
            Path filePath = Path.of(pathInPackage);
            File spriteFile = FileLoading.fetchDataFile(filePath, packageFolderPath);

            if (spriteFile == null) {
                String report = "Image file not found: " + filePath;
                JOptionPane.showMessageDialog(null,
                        report,
                        StringValues.FILE_LOADING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
                FileNotFoundException notFoundException = new FileNotFoundException(report);
                notFoundException.printStackTrace();
                return;
            }

            Sprite newSprite = FileLoading.loadSprite(spriteFile);
            setter.accept(newSprite);
        }
    }

    public static String translateIntegerValue(Supplier<Integer> getter) {
        String notInitialized = StringValues.NOT_INITIALIZED;
        int value = getter.get();
        String textResult;
        if (value == -1) {
            textResult = notInitialized;
        } else {
            textResult = String.valueOf(value);
        }
        return textResult;
    }

    public static String computeRelativePathFromPackage(Path fullPath) {
        Path coreDataFolder = SettingsManager.getCoreFolderPath();
        List<Path> otherModFolders = SettingsManager.getAllModFolders();
        String relativePathFromCore = Utility.findRelativePath(coreDataFolder, fullPath);

        if (relativePathFromCore != null) {
            return relativePathFromCore;
        }

        for (Path modFolder : otherModFolders) {
            String relativePathFromMod = Utility.findRelativePath(modFolder, fullPath);
            if (relativePathFromMod != null) {
                return relativePathFromMod;
            }
        }

        return fullPath.toString();
    }

    private static String findRelativePath(Path baseFolder, Path fullPath) {
        Path relativePath = baseFolder.relativize(fullPath);
        if (!relativePath.isAbsolute()) {
            return relativePath.toString().replace("\\", "/");
        }
        return null;
    }

    public static String formatDouble(double value) {
        if (value % 1 == 0) {
            return String.format("%8d", (int) value);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.###");
            String formattedValue = decimalFormat.format(value);
            return String.format("%8s", formattedValue);
        }
    }

    public static boolean areDoublesEqual(double first, double second) {
        return Math.abs(first - second) < 0.005;
    }

    public static String getFilenameWithoutExtension(String filename) {
        Path path = Paths.get(filename);
        Matcher matcher = FILE_EXTENSION.matcher(path.getFileName().toString());
        return matcher.replaceFirst("");
    }

    public static void flipPointHorizontally(WorldPoint toFlip, WorldPoint anchor) {
        Utility.flipPointHorizontally(toFlip.getPosition(), anchor.getPosition());
        if (toFlip instanceof AngledPoint angledPoint) {
            double flipped = Utility.flipAngle(angledPoint.getAngle());
            angledPoint.setAngle(flipped);
        }
    }

    private static void flipPointHorizontally(Point2D toFlip, Point2D anchor) {
        double anchorX = anchor.getX();
        double deltaX = toFlip.getX() - anchorX;
        double newX = anchorX - deltaX;
        toFlip.setLocation(newX, toFlip.getY());
    }

}
