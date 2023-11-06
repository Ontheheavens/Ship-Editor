package oth.shipeditor.components.logging;

import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        logger.setBorder(new EmptyBorder(2, 2, 2, 2));
        logger.setForeground(Themes.getTextColor());
        logger.setBackground(Themes.getListBackgroundColor());
        logger.setFont(Utility.getDefaultFont());
        scrollPane = new JScrollPane(logger);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public static void append(String formattedMessage) {
        if (logger != null) {
            logger.append(formattedMessage);
            if (scrollPane != null) {
                LogsPanel.scrollToBottom();
            }
        }
    }

    public static void scrollToBottom() {
        Rectangle visible = logger.getVisibleRect();
        Rectangle bounds = logger.getBounds();

        visible.y = bounds.height - visible.height;
        visible.x = 0;
        logger.scrollRectToVisible(visible);
    }

}
