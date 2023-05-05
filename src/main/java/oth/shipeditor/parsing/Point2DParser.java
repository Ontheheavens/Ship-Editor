package oth.shipeditor.parsing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class Point2DParser extends JsonDeserializer<Point2D.Double> {

    @Override
    public Point2D.Double deserialize(JsonParser p, DeserializationContext context) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        double x = node.get(0).asDouble();
        double y = node.get(1).asDouble();
        return new Point2D.Double(x, y);
    }

}
