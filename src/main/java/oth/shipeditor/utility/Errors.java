package oth.shipeditor.utility;

import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 21.09.2023
 */
public final class Errors {

    private Errors() {
    }

    public static void showFileError(String message, Throwable exception) {
        JOptionPane.showMessageDialog(null, message, StringValues.FILE_LOADING_ERROR,
                JOptionPane.ERROR_MESSAGE);
        exception.printStackTrace();
    }

}
