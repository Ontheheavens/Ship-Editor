package oth.shipeditor.parsing.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 23.10.2023
 */
public class BaseNumberSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value % 1 == 0) {
            gen.writeNumber(value.intValue());
        } else {
            gen.writeNumber(value);
        }
    }

}
