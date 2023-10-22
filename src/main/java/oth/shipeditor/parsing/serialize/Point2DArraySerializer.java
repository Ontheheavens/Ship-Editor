package oth.shipeditor.parsing.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import oth.shipeditor.persistence.BasicPrettyPrinter;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public class Point2DArraySerializer extends JsonSerializer<Point2D.Double[]> {

    @Override
    public void serialize(Point2D.Double[] value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        PrettyPrinter prettyPrinter = gen.getPrettyPrinter();
        prettyPrinter.beforeArrayValues(gen);

        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        int length = value.length;
        for (int i = 0; i < length; i++) {
            Point2D.Double point = value[i];

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

            if (i == length - 1) {
                gen.writeRaw(BasicPrettyPrinter.BLANK_LINE);
                gen.writeRaw(BasicPrettyPrinter.INDENT);
                gen.writeRaw("# " + length + " points total.");

                gen.writeRaw(BasicPrettyPrinter.BLANK_LINE);
                gen.writeRaw(BasicPrettyPrinter.INDENT);
            } else {
                prettyPrinter.writeArrayValueSeparator(gen);
            }
            if ((i + 1) % 5 == 0 && i != length - 1) {
                gen.writeRaw(BasicPrettyPrinter.BLANK_LINE);
                gen.writeRaw(BasicPrettyPrinter.INDENT);
                gen.writeRaw(BasicPrettyPrinter.INDENT);
            }
        }
        gen.writeEndArray();
    }

}
