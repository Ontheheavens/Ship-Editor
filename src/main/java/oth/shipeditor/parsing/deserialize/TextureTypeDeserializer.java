package oth.shipeditor.parsing.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
public class TextureTypeDeserializer extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.isExpectedStartArrayToken()) {
            // Deserialize array.
            return p.readValueAs(new TypeReference<List<String>>() {});
        } else {
            // Deserialize single value as a list.
            String singleValue = p.getValueAsString();
            List<String> list = new ArrayList<>();
            list.add(singleValue);
            return list;
        }
    }

}