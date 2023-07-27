package oth.shipeditor.utility.components.dialog;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public class AngleChangeDialog extends NumberChangeDialog {

    AngleChangeDialog(double original) {
        super(original);
    }

    @Override
    protected JLabel createOriginalLabel() {
        return new JLabel(getOriginalNumber() + "° ");
    }

    @Override
    protected SpinnerNumberModel createSpinnerModel() {
        double min = 0.0d;
        double max = 359.0d;
        double step = 1.0d;
        return new SpinnerNumberModel(getOriginalNumber(), min, max, step);
    }

}
