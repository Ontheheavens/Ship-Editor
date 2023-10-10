package oth.shipeditor.components.logging;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 10.10.2023
 */
@Getter
public class LogsPanel extends JPanel {

    @Getter
    private static final JTextArea logger;

    static {
        logger = new JTextArea();
        logger.setEditable(false);
    }

    public LogsPanel() {
        this.setLayout(new BorderLayout());
        JScrollPane scroller = new JScrollPane(logger);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        this.add(scroller, BorderLayout.CENTER);
    }

}
