package oth.shipeditor.parsing.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class Point2DDeserializer extends JsonDeserializer<Point2D.Double> {

    @Override
    public Point2D.Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);
        double x = node.get(0).asDouble();
        double y = node.get(1).asDouble();
        return new Point2D.Double(x, y);
    }

}
