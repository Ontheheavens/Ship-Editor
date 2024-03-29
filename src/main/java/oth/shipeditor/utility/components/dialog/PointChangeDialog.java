package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.Spinners;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
class PointChangeDialog extends JPanel {

    private final Point2D originalPosition;
    private SpinnerNumberModel spinnerModelX;
    private SpinnerNumberModel spinnerModelY;

    PointChangeDialog(Point2D original) {
        this.originalPosition = original;
        this.setLayout(new BorderLayout());
        this.add(createTopPanel(), BorderLayout.PAGE_START);
        this.add(createSpinnerPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JLabel originalPositionLabel = new JLabel(originalPosition.getX() + ", " + originalPosition.getY() + " ");

        JPanel positionContainer = ComponentUtilities.createBoxLabelPanel("Original position: ",
                originalPositionLabel, 0);

        positionContainer.setBorder(new EmptyBorder(0, 0, 4, 0));
        topPanel.add(positionContainer);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        topPanel.add(separator);
        return topPanel;
    }

    private JPanel createSpinnerPanel() {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        double step = 0.005;

        spinnerModelX = new SpinnerNumberModel(originalPosition.getX(), min, max, step);
        spinnerModelY = new SpinnerNumberModel(originalPosition.getY(), min, max, step);

        String labelTextX = "X coordinate (World):";
        String labelTextY = "Y coordinate (World):";

        return Spinners.createTwinSpinnerPanel(spinnerModelX, spinnerModelY,
                labelTextX, labelTextY);
    }

    Point2D getUpdatedPosition() {
        Number xNumber = spinnerModelX.getNumber();
        double x = xNumber.doubleValue();
        Number yNumber = spinnerModelY.getNumber();
        double y = yNumber.doubleValue();
        return new Point2D.Double(x, y);
    }

}
