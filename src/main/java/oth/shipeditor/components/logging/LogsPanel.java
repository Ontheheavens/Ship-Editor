package oth.shipeditor.components.logging;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 10.10.2023
 */
public class LogsPanel extends JPanel {

    private static final JTextArea logger;

    private static JScrollPane scrollPane;

    static {
        logger = new JTextArea();
        logger.setEditable(false);
    }

    public LogsPanel() {
        this.setLayout(new BorderLayout());
        scrollPane = new JScrollPane(logger);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public static void append(String formattedMessage) {
        if (logger != null) {
            logger.append(formattedMessage);
            if (scrollPane != null) {
                Rectangle visible = logger.getVisibleRect();
                Rectangle bounds = logger.getBounds();

                visible.y = bounds.height - visible.height;
                visible.x = 0;
                logger.scrollRectToVisible(visible);
            }
        }
    }

}
