package oth.shipeditor.components.logging;

import lombok.extern.log4j.Log4j2;

import java.io.PrintStream;

/**
 * @author Ontheheavens
 * @since 21.10.2023
 */
@Log4j2
public final class StandardOutputRedirector {

    private StandardOutputRedirector() {}

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void redirectStandardStreams() {
        PrintStream out = System.out;
        System.setOut(StandardOutputRedirector.createLoggingProxy(out));
        PrintStream err = System.err;
        System.setErr(StandardOutputRedirector.createLoggingProxy(err));
    }

    @SuppressWarnings("ImplicitDefaultCharsetUsage")
    private static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String s) {
                realPrintStream.print(s);
                var textArea = LogsPanel.getLogger();
                textArea.append(s + System.lineSeparator());
            }
        };
    }

}
