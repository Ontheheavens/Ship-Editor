package oth.shipeditor.parsing.serialize.points;

import com.fasterxml.jackson.core.JsonGenerator;
import oth.shipeditor.persistence.BasicPrettyPrinter;

import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 23.10.2023
 */
public class EngineLocationSerializer extends Point2DSerializer {

    @Override
    protected void writeClosingIndentation(JsonGenerator gen) throws IOException {
        super.writeClosingIndentation(gen);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
    }

}
