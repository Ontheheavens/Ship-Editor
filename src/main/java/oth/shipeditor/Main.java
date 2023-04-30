package oth.shipeditor;

import javax.swing.*;

public class Main {

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        PrimaryWindow starter = PrimaryWindow.getInstance();
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // Creating and showing this application's GUI.
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

}