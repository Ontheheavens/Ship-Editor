package oth.shipeditor;

/*
 * HelloWorldSwing.java requires no other files.
 */

import javax.swing.*;
import java.awt.*;

public class Main {

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Ship Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(640, 480));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Create and add the coordinate plane panel to the frame
        CoordinatePlanePanel planePanel = new CoordinatePlanePanel();

        frame.getContentPane().add(planePanel, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = menuBar.add(new JMenu("TEST"));
        JMenuItem item = menu.add(new JMenuItem("TEST"));
        JMenuItem resetItem = menu.add(new JMenuItem("Reset Zoom"));
        resetItem.addActionListener(e -> planePanel.resetZoom());
        frame.setJMenuBar(menuBar);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
}