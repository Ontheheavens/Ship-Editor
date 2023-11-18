package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.logging.StandardOutputRedirector;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 21.09.2023
 */
@Log4j2
public final class Errors {

    private Errors() {
    }

    public static void initGlobalHandler() {
        Thread.UncaughtExceptionHandler globalExceptionHandler = new Handler();
        Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
    }

    public static void showFileError(String message) {
        Errors.showFileError(message, null);
    }

    public static void showFileError(String message, Throwable exception) {
        Object[] options = {"OK", "Hide file errors"};
        int result = JOptionPane.showOptionDialog(
                null,
                message,
                StringValues.FILE_LOADING_ERROR,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]);

        if (result == 1) {
            Settings settings = SettingsManager.getSettings();
            settings.setShowLoadingErrors(false);
            log.info("File error pop-ups are disabled by user.");
        }

        if (exception != null) {
            Errors.printToStream(exception);
        }
    }

    public static void printToStream(Throwable throwable) {
        throwable.printStackTrace(StandardOutputRedirector.getErrorStreamProxy());
    }

    private static class Handler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            Errors.printToStream(e);
        }
    }

}
