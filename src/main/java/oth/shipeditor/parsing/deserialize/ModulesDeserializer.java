package oth.shipeditor.parsing.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
public class ModulesDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        Map<String, String> moduleMap = new HashMap<>();

        if (node.isArray()) {
            for (JsonNode entry : node) {
                if (entry.isObject() && entry.size() == 1) {
                    Iterator<String> stringIterator = entry.fieldNames();
                    String key = stringIterator.next();
                    String value = entry.get(key).asText();
                    moduleMap.put(key, value);
                }
            }
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> entryIterator = node.fields();
            entryIterator.forEachRemaining(entry -> {
                JsonNode entryValue = entry.getValue();
                moduleMap.put(entry.getKey(), entryValue.asText());
            });
        }

        return moduleMap;
    }
}
