package oth.shipeditor.components;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.LoadingActionFired;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 28.10.2023
 */
public class ProgressBarPanel extends JPanel {

    private final JProgressBar progressBar;

    ProgressBarPanel() {
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setIndeterminate(true);
        EventBus.subscribe(event -> {
            if (event instanceof LoadingActionFired checked) {
                if (checked.started()) {
                    this.add(progressBar);
                } else {
                    this.remove(progressBar);
                }
                this.revalidate();
                this.repaint();
            }
        });
    }

}
