package oth.shipeditor.parsing.serialize.points;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import oth.shipeditor.parsing.serialize.SerializationUtilities;
import oth.shipeditor.persistence.BasicPrettyPrinter;

import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 23.10.2023
 */
public class Point2DSerializer extends JsonSerializer<Point2D.Double> {

    @Override
    public void serialize(Point2D.Double value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        PrettyPrinter prettyPrinter = gen.getPrettyPrinter();
        prettyPrinter.beforeArrayValues(gen);

        SerializationUtilities.writePoint2DForSingle(value, gen);

        writeClosingIndentation(gen);

        gen.writeEndArray();
    }

    protected void writeClosingIndentation(JsonGenerator gen) throws IOException {
        gen.writeRaw(BasicPrettyPrinter.LINEFEED);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
    }

}
