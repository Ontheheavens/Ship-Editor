package oth.shipeditor.utility;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.representation.Skin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static String getSkinFileName(ShipCSVEntry checked, Skin activeSkin) {
        String skinFileName = "";
        Map<String, Skin> skins = checked.getSkins();
        for (String skinName : skins.keySet()) {
            Skin skin = skins.get(skinName);
            if (skin.equals(activeSkin)) {
                skinFileName = skinName;
                break;
            }
        }
        return skinFileName;
    }

    /**
     * Target CSV file is expected to have a header row and an ID column designated in said header.
     * @param path address of the target file.
     * @return List of rows where each row is a Map of string keys and string values.
     */
    public static java.util.List<Map<String, String>> parseCSVTable(Path path) {
        CsvMapper csvMapper = new CsvMapper();

        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader().withComments();
        File csvFile = path.toFile();
        List<Map<String, String>> csvData = new ArrayList<>();
        try (MappingIterator<Map<String, String>> iterator = csvMapper.readerFor(Map.class)
                .with(csvSchema)
                .readValues(csvFile)) {
            while (iterator.hasNext()) {
                Map<String, String> row = iterator.next();
                String id = row.get(StringConstants.ID);
                String name = row.get("name");
                if (!id.isEmpty() && !name.startsWith("#") && row.size() > 10) {
                    // We are skipping a row if ID is missing or if row is commented out.
                    // Size of 10 is a hack intended to exclude remainders of commented lines.
                    csvData.add(row);
                }
            }
        } catch (IOException exception) {
            log.error("Data CSV loading failed!");
            exception.printStackTrace();
        }
        return csvData;
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
