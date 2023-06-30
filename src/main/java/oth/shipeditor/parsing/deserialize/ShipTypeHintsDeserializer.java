package oth.shipeditor.parsing.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.representation.ShipTypeHints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 30.06.2023
 */
@Log4j2
public class ShipTypeHintsDeserializer extends JsonDeserializer<ShipTypeHints[]> {

    @Override
    public ShipTypeHints[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);
        List<ShipTypeHints> hintsList = new ArrayList<>();
        for (JsonNode hintNode : node) {
            log.info("TESSSSSST: " + hintNode);
            String hintValue = hintNode.textValue();
            ShipTypeHints hint = ShipTypeHints.valueOf(hintValue);
            hintsList.add(hint);
        }
        return hintsList.toArray(new ShipTypeHints[0]);
    }

}
