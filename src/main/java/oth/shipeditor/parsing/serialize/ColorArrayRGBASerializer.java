package oth.shipeditor.parsing.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.awt.*;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
public class ColorArrayRGBASerializer extends StdSerializer<Color> {

    public ColorArrayRGBASerializer() {
        super(Color.class);
    }

    protected ColorArrayRGBASerializer(Class<Color> t) {
        super(t);
    }

    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(value.getRed());
        gen.writeNumber(value.getGreen());
        gen.writeNumber(value.getBlue());
        gen.writeNumber(value.getAlpha());
        gen.writeEndArray();
    }

}
