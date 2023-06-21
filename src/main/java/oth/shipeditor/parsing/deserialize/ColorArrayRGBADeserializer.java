package oth.shipeditor.parsing.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.awt.*;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
public class ColorArrayRGBADeserializer extends JsonDeserializer<Color> {

    // TODO: Introduce Color string serializer/deserializer as well for coversColor field.

    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        int[] rgbaValues = p.readValueAs(int[].class);
        if (rgbaValues.length != 4) {
            throw new IOException("Invalid RGBA values: " + p.currentToken());
        }

        int red = rgbaValues[0];
        int green = rgbaValues[1];
        int blue = rgbaValues[2];
        int alpha = rgbaValues[3];

        return new Color(red, green, blue, alpha);
    }

}
