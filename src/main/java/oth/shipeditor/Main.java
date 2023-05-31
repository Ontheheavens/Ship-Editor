package oth.shipeditor;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Look-and-feel setup failed!");
            }
            Window window = Window.getFrame();
            window.showGUI();
        });
    }

}
