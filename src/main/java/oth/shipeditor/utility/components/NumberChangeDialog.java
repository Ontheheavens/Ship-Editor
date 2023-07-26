package oth.shipeditor.utility.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 26.07.2023
 */
class NumberChangeDialog extends JPanel {

    private final Number originalNumber;

    private SpinnerNumberModel spinnerModel;

    NumberChangeDialog(Number original) {
        this.originalNumber = original;
        this.setLayout(new BorderLayout());
        this.add(createTopPanel(), BorderLayout.PAGE_START);
        this.add(createSpinnerPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JLabel originalNumberLabel = new JLabel(originalNumber.doubleValue() + " ");

        JPanel numberContainer = ComponentUtilities.createBoxLabelPanel("Original value: ",
                originalNumberLabel, 0);

        numberContainer.setBorder(new EmptyBorder(0, 0, 4, 0));
        topPanel.add(numberContainer);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        topPanel.add(separator);
        return topPanel;
    }

    private JPanel createSpinnerPanel() {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        double step = 0.005;
        spinnerModel = new SpinnerNumberModel(originalNumber.doubleValue(), min, max, step);

        JLabel label = new JLabel("New value:");

        JSpinner spinner = new JSpinner(spinnerModel);

        JPanel container = new JPanel(new GridLayout(1, 2));
        container.setBorder(new EmptyBorder(4, 0, 0, 0));
        container.add(label);
        container.add(spinner);
        return container;
    }

    double getUpdatedValue() {
        Number number = (spinnerModel.getNumber());
        return number.doubleValue();
    }

}
