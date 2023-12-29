package oth.shipeditor.utility.components.widgets;

import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 07.11.2023
 */
public final class Spinners {

    private Spinners() {
    }

    /**
     * Default values for spinner are set to 0/360.
     */
    public static void addLabelWithDegreeSpinner(JPanel container, String labelText,
                                                 Consumer<Double> spinnerEffect, int y) {
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0,
                0, 360, 0.5d);
        Spinners.addLabelWithSpinner(container, labelText, spinnerEffect, spinnerNumberModel, y);
    }

    /**
     * @param container expected to have GridBagLayout.
     * @param y         vertical grid position in layout, 0 corresponds to first/top.
     */
    private static JSpinner addLabelWithSpinner(JPanel container, String labelText,
                                                Consumer<Double> spinnerEffect,
                                                SpinnerNumberModel spinnerNumberModel, int y) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(3, 3, 0, 3);
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_START;

        JLabel selectorLabel = new JLabel(labelText);
        container.add(selectorLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.gridy = y;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 3, 0, 2);
        constraints.anchor = GridBagConstraints.LINE_END;

        JSpinner spinner =  Spinners.createWheelable(spinnerNumberModel);
        if (spinnerEffect != null) {
            spinner.addChangeListener(e -> {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();
                spinnerEffect.accept(current);
            });
        }

        container.add(spinner, constraints);
        return spinner;
    }

    static TwinSpinnerPanel createLocationSpinners(Point2D initial,
                                                   Supplier<Point2D> pointGetter,
                                                   Consumer<Point2D> pointSetter) {
        return Spinners.createLocationSpinners(initial, pointGetter,
                pointSetter, StringValues.X_COORDINATE, StringValues.Y_COORDINATE);
    }

    public static TwinSpinnerPanel createLocationSpinners(Point2D initial,
                                                          Supplier<Point2D> pointGetter,
                                                          Consumer<Point2D> pointSetter,
                                                          String labelX, String labelY) {
        return Spinners.createLocationSpinners(initial, pointGetter, pointSetter, labelX, labelY, 0.5d);
    }

    public static TwinSpinnerPanel createLocationSpinners(Point2D initial,
                                                          Supplier<Point2D> pointGetter,
                                                          Consumer<Point2D> pointSetter,
                                                          String labelX, String labelY, double stepSize) {
        SpinnerNumberModel modelX = new SpinnerNumberModel(initial.getX(),
                Integer.MIN_VALUE, Integer.MAX_VALUE, stepSize);
        Consumer<Double> spinnerEffectX = value -> {
            Point2D original = pointGetter.get();
            if (original != null) {
                Point2D changed = new Point2D.Double(value, original.getY());
                pointSetter.accept(changed);
            }

        };

        SpinnerNumberModel modelY = new SpinnerNumberModel(initial.getY(),
                Integer.MIN_VALUE, Integer.MAX_VALUE, stepSize);
        Consumer<Double> spinnerEffectY = value -> {
            Point2D original = pointGetter.get();
            if (original != null) {
                Point2D changed = new Point2D.Double(original.getX(), value);
                pointSetter.accept(changed);
            }
        };

        return Spinners.createTwinSpinnerPanel(modelX, modelY,
                spinnerEffectX, spinnerEffectY,
                labelX, labelY);
    }

    public static TwinSpinnerPanel createTwinSpinnerPanel(SpinnerNumberModel firstModel, SpinnerNumberModel secondModel,
                                                String firstLabelText, String secondLabelText) {
        return Spinners.createTwinSpinnerPanel(firstModel, secondModel,
                null, null, firstLabelText, secondLabelText);
    }

    private static TwinSpinnerPanel createTwinSpinnerPanel(SpinnerNumberModel firstModel, SpinnerNumberModel secondModel,
                                                 Consumer<Double> firstSpinnerEffect, Consumer<Double> secondSpinnerEffect,
                                                 String firstLabelText, String secondLabelText) {
        TwinSpinnerPanel container = new TwinSpinnerPanel();

        JSpinner firstSpinner = Spinners.addLabelWithSpinner(container,
                firstLabelText, firstSpinnerEffect, firstModel, 0);
        container.setFirstSpinner(firstSpinner);
        JSpinner secondSpinner = Spinners.addLabelWithSpinner(container,
                secondLabelText, secondSpinnerEffect, secondModel, 1);
        container.setSecondSpinner(secondSpinner);

        return container;
    }

    public static JSpinner createWheelable(SpinnerNumberModel model) {
        return Spinners.createWheelable(model, IncrementType.CHUNK);
    }

    public static JSpinner createUnaryIntegerWheelable(SpinnerNumberModel model) {
        JSpinner spinner = Spinners.createWheelable(model, IncrementType.UNARY);
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) spinner.getEditor();
        JFormattedTextField textField = spinnerEditor.getTextField();
        textField.setEditable(true);
        textField.setColumns(1);
        return spinner;
    }

    /**
     * @param model expected to operate exclusively with Double numbers!
     */
    public static JSpinner createWheelable(SpinnerNumberModel model, IncrementType type) {
        JSpinner spinner = new JSpinner(model);
        spinner.addMouseWheelListener(e -> {
            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                return;
            }
            double value = (Double) spinner.getValue();
            double scrollAmount = e.getUnitsToScroll();
            double newValue = value;
            switch (type) {
                case UNARY -> {
                    // Compare the scroll amount to ensure we always change only by +1 or -1.
                    int adjustedScroll = Double.compare(scrollAmount, 0);
                    newValue = value - adjustedScroll;
                }
                case CHUNK -> newValue = value - scrollAmount;
            }
            newValue = Math.min((Double) model.getMaximum(), Math.max((Double) model.getMinimum(), newValue));
            spinner.setValue(newValue);
        });
        return spinner;
    }

}
