package org.example;

/*
 * HelloWorldSwing.java requires no other files.
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

public class Main {

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(640, 480));
        frame.setLocationRelativeTo(null);

        // Create and add the coordinate plane panel to the frame
        CoordinatePlanePanel planePanel = new CoordinatePlanePanel();

        final BufferedImage image;

        @SuppressWarnings("SpellCheckingInspection") String imagePath = "C:\\Games\\msdr_drone_shield.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JPanel pane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                float scaleMult = 15f;
                Image scaled = image.getScaledInstance((int) (image.getWidth() * scaleMult),
                        (int) (image.getHeight() * scaleMult), 0);
                g.drawImage(scaled, 0, 0, null);
            }
        };

//        frame.getContentPane().add(pane);

        frame.getContentPane().add(planePanel);

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