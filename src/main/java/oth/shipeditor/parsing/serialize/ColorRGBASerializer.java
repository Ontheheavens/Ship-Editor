package oth.shipeditor.parsing.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.awt.*;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
public class ColorRGBASerializer extends JsonSerializer<Color> {

    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(value.getRed());
        gen.writeNumber(value.getGreen());
        gen.writeNumber(value.getBlue());
        gen.writeNumber(value.getAlpha());
        gen.writeEndArray();
    }

}
