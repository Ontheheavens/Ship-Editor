package oth.shipeditor.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public class BasicPrettyPrinter extends DefaultPrettyPrinter {

    public static final String BLANK_LINE = DefaultIndenter.SYS_LF;
    private static final char ARRAY_END = ']';

    public static final String INDENT = "    ";

    @Override
    public BasicPrettyPrinter createInstance() {
        BasicPrettyPrinter basicPrettyPrinter = new BasicPrettyPrinter();

        DefaultIndenter indenter = new DefaultIndenter(BasicPrettyPrinter.INDENT, BasicPrettyPrinter.BLANK_LINE);
        basicPrettyPrinter.indentArraysWith(indenter);
        basicPrettyPrinter.indentObjectsWith(indenter);

        return basicPrettyPrinter;
    }


    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw(ARRAY_END);
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(_separators.getObjectFieldValueSeparator());
        g.writeRaw(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
    }

}
