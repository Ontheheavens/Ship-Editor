package oth.shipeditor;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
public class Main {

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // Creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
                throw new RuntimeException();
            }
            Window window = Window.getFrame();
            window.showGUI();
        });
    }

}
