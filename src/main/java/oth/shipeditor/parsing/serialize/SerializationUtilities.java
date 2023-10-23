package oth.shipeditor.parsing.serialize;

import com.fasterxml.jackson.core.JsonGenerator;

import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 23.10.2023
 */
public final class SerializationUtilities {

    private SerializationUtilities() {
    }

    public static void writePoint2DForArray(Point2D point, JsonGenerator gen) throws IOException {
        String resultX;
        String resultY;

        double pointX = point.getX();
        double pointY = point.getY();

        if (pointX % 1 == 0) {
            resultX = String.format("%7d", (int) pointX);
        } else {
            resultX = String.format("%7.1f", pointX);
        }

        if (pointY % 1 == 0) {
            resultY = String.format("%7d", (int) pointY);
        } else {
            resultY = String.format("%7.1f", pointY);
        }

        gen.writeRaw(resultX);
        gen.writeRaw(", " + resultY);
    }

    public static void writePoint2DForSingle(Point2D point, JsonGenerator gen) throws IOException {
        String resultX;
        String resultY;

        double pointX = point.getX();
        double pointY = point.getY();

        if (pointX % 1 == 0) {
            resultX = String.valueOf((int)pointX);
        } else {
            resultX = String.valueOf(pointX);
        }

        if (pointY % 1 == 0) {
            resultY =  String.valueOf((int)pointY);
        } else {
            resultY = String.valueOf(pointY);
        }

        gen.writeRaw(resultX);
        gen.writeRaw(", " + resultY);
    }

}
