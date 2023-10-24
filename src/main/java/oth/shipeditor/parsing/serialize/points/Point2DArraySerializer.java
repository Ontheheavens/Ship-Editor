package oth.shipeditor.parsing.serialize.points;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import oth.shipeditor.parsing.serialize.SerializationUtilities;
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

            SerializationUtilities.writePoint2DForArray(point, gen);

            if (i == length - 1) {
                writeClosure(gen, length);
            } else {
                prettyPrinter.writeArrayValueSeparator(gen);
            }

            if ((i + 1) % 5 == 0 && i != length - 1) {
                gen.writeRaw(BasicPrettyPrinter.LINEFEED);
                gen.writeRaw(BasicPrettyPrinter.INDENT);
                gen.writeRaw(BasicPrettyPrinter.INDENT);
            }
        }
        gen.writeEndArray();
    }

    protected void writeClosure(JsonGenerator gen, int length) throws IOException {
        if (length > 10) {
            gen.writeRaw(BasicPrettyPrinter.LINEFEED);
            gen.writeRaw(BasicPrettyPrinter.INDENT);
            gen.writeRaw("# " + length + " points total.");
        }

        gen.writeRaw(BasicPrettyPrinter.LINEFEED);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
    }

}
