package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.logging.StandardOutputRedirector;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

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

    public static void showFileOpeningError(File toOpen, Throwable exception) {
        String filePath = toOpen.getAbsolutePath();

        log.error("Failed to open {} in Explorer!", filePath);
        JOptionPane.showMessageDialog(null,
                "Failed to open file in Explorer, exception thrown at: " + filePath,
                StringValues.FILE_LOADING_ERROR,
                JOptionPane.ERROR_MESSAGE);
        Errors.printToStream(exception);
    }

    static void showSpriteNotFound(String filePath) {
        String report = "Image file not found: " + filePath;
        JOptionPane.showMessageDialog(null,
                report,
                StringValues.FILE_LOADING_ERROR,
                JOptionPane.ERROR_MESSAGE);
        FileNotFoundException notFoundException = new FileNotFoundException(report);
        Errors.printToStream(notFoundException);
    }

    public static void printToStream(Throwable throwable) {
        throwable.printStackTrace(StandardOutputRedirector.getErrorStreamProxy());
    }

    private static class Handler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            log.error("Exception caught with global handler!", e);
            Errors.printToStream(e);
        }
    }

}
