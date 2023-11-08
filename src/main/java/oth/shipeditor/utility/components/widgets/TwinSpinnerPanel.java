package oth.shipeditor.utility.components.widgets;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

/**
 * Container for holding and accessing spinners. Callers are responsible for setting references.
 * Uses GridBagLayout by default.
 * @author Ontheheavens
 * @since 08.11.2023
 */
@Getter @Setter
public class TwinSpinnerPanel extends JPanel {

    private JSpinner firstSpinner;

    private JSpinner secondSpinner;

    TwinSpinnerPanel() {
        this.setLayout(new GridBagLayout());
    }

    public void clear() {
        firstSpinner.setValue(0.0d);
        secondSpinner.setValue(0.0d);
    }

    public void disable() {
        firstSpinner.setEnabled(false);
        secondSpinner.setEnabled(false);
    }

    public void enable() {
        firstSpinner.setEnabled(true);
        secondSpinner.setEnabled(true);
    }

}
