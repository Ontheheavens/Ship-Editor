package oth.shipeditor.parsing.serialize.points;

import com.fasterxml.jackson.core.JsonGenerator;
import oth.shipeditor.persistence.BasicPrettyPrinter;

import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 24.10.2023
 */
public class SlotLocationsSerializer extends Point2DArraySerializer {

    @Override
    protected void writeClosure(JsonGenerator gen, int length) throws IOException {
        gen.writeRaw(BasicPrettyPrinter.LINEFEED);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
        gen.writeRaw(BasicPrettyPrinter.INDENT);
    }

}
