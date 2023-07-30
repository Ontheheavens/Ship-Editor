package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public class RadiusChangeDialog extends NumberChangeDialog{

    RadiusChangeDialog(double original) {
        super(original);
    }

    @Override
    protected JLabel createOriginalLabel() {
        return new JLabel(Utility.round(getOriginalNumber(), 5) + " " + StringValues.PIXELS + " ");
    }

    @Override
    protected SpinnerNumberModel createSpinnerModel() {
        double min = 0.0d;
        double max = Double.POSITIVE_INFINITY;
        double step = 0.005;
        return new SpinnerNumberModel(getOriginalNumber(), min, max, step);
    }

}
