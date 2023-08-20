package oth.shipeditor.components.instrument.ship.engines;

import lombok.Getter;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.EngineSlotPainter;
import oth.shipeditor.utility.Size2D;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineDataPanel extends JPanel {

    @Getter
    private final EnginePoint selected;

    EngineDataPanel(EnginePoint point) {
        this.selected = point;
        this.setLayout(new GridBagLayout());
        this.addContent();
    }

    private void addContent() {
        this.addAngleController();
        this.addWidthController();
        this.addLengthController();
    }

    private void addAngleController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParentLayer();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            enginePainter.changePointAngleWithMirrorCheck(selected, current);
        };
        Supplier<Double> angle = null;
        if (selected != null) {
            angle = selected::getAngle;
        }
        this.addValueSelector("Engine angle:", angle,
                -360, 360, action, 0);
    }

    private void addWidthController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParentLayer();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            double currentLength = selected.getLength();
            Size2D newSize = new Size2D(current, currentLength);
            enginePainter.changeEngineSizeWithMirrorCheck(selected, newSize);
        };
        Supplier<Double> width = null;
        if (selected != null) {
            width = selected::getWidth;
        }
        this.addValueSelector("Engine width:", width,
                0.5d, Double.MAX_VALUE, action, 1);
    }

    private void addLengthController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParentLayer();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            double selectedWidth = selected.getWidth();
            Size2D newSize = new Size2D(selectedWidth, current);
            enginePainter.changeEngineSizeWithMirrorCheck(selected, newSize);
        };
        Supplier<Double> length = null;
        if (selected != null) {
            length = selected::getLength;
        }
        this.addValueSelector("Engine length:", length,
                0.5d, Double.MAX_VALUE, action, 2);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private void addValueSelector(String selectorLabelText, Supplier<Double> currentValue, double minValue,
                                  double maxValue, Consumer<Double> action, int position) {
        JLabel selectorLabel = new JLabel(selectorLabelText);

        String tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        selectorLabel.setToolTipText(tooltip);

        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), position);
            return;
        }
        double currentInput = currentValue.get();
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(currentInput,
                minValue, maxValue, 0.5d);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();
                action.accept(current);
                spinner.removeChangeListener(this);
            }
        });
        spinner.addMouseWheelListener(e -> {
            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                return;
            }
            double value = (Double) spinner.getValue();
            double newValue = value - e.getUnitsToScroll();
            newValue = Math.min(maxValue, Math.max(minValue, newValue));
            spinner.setValue(newValue);
        });

        ComponentUtilities.addLabelAndComponent(this, selectorLabel, spinner, position);
    }

}
