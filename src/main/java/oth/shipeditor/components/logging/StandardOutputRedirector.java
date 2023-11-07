package oth.shipeditor.components.logging;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.PrintStream;

/**
 * @author Ontheheavens
 * @since 21.10.2023
 */
@Log4j2
public final class StandardOutputRedirector {

    @Getter
    private static PrintStream outStreamProxy;

    @Getter
    private static PrintStream errorStreamProxy;

    private StandardOutputRedirector() {}

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void redirectStandardStreams() {
        PrintStream out = System.out;
        outStreamProxy = StandardOutputRedirector.createLoggingProxy(out);
        System.setOut(outStreamProxy);

        PrintStream err = System.err;
        errorStreamProxy = StandardOutputRedirector.createLoggingProxy(err);
        System.setErr(errorStreamProxy);
    }

    @SuppressWarnings("ImplicitDefaultCharsetUsage")
    private static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String s) {
                realPrintStream.print(s);
                LogsPanel.append(s + System.lineSeparator());
            }
        };
    }

}
