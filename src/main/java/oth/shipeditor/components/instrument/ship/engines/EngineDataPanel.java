package oth.shipeditor.components.instrument.ship.engines;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.components.viewer.entities.engine.EngineDataOverride;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.EngineSlotPainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.ship.EngineStyle;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.Map;
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
        this.addContrailController();
        this.addStyleSelector();
    }

    private void addAngleController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParent();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            enginePainter.changePointAngleWithMirrorCheck(selected, current);
        };
        Supplier<Double> angle = null;
        if (selected != null) {
            angle = selected::getAngle;
        }
        JComponent selector = this.addValueSelector("Engine angle:", angle,
                -360, 360, action, 0);

        EngineDataOverride skinOverride = null;
        if (selected != null) {
            skinOverride = selected.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getAngle() != null) {
            selector.setToolTipText("Locked: engine angle overridden by skin");
            selector.setEnabled(false);
        }
    }

    private void addWidthController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParent();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            double currentLength = selected.getLength();
            Size2D newSize = new Size2D(current, currentLength);
            enginePainter.changeEngineSizeWithMirrorCheck(selected, newSize);
        };
        Supplier<Double> width = null;
        if (selected != null) {
            width = selected::getWidth;
        }
        JComponent selector = this.addValueSelector("Engine width:", width,
                0, Double.MAX_VALUE, action, 1);

        EngineDataOverride skinOverride = null;
        if (selected != null) {
            skinOverride = selected.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getWidth() != null) {
            selector.setToolTipText("Locked: engine width overridden by skin");
            selector.setEnabled(false);
        }
    }

    private void addLengthController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParent();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            double selectedWidth = selected.getWidth();
            Size2D newSize = new Size2D(selectedWidth, current);
            enginePainter.changeEngineSizeWithMirrorCheck(selected, newSize);
        };
        Supplier<Double> length = null;
        if (selected != null) {
            length = selected::getLength;
        }
        JComponent selector = this.addValueSelector("Engine length:", length,
                0, Double.MAX_VALUE, action, 2);

        EngineDataOverride skinOverride = null;
        if (selected != null) {
            skinOverride = selected.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getLength() != null) {
            selector.setToolTipText("Locked: engine length overridden by skin");
            selector.setEnabled(false);
        }
    }

    private void addContrailController() {
        Consumer<Double> action = current -> {
            ShipPainter slotParent = (ShipPainter) selected.getParent();
            EngineSlotPainter enginePainter = slotParent.getEnginePainter();
            enginePainter.changeEngineContrailWithMirrorCheck(selected, (int) Math.round(current));
        };
        Supplier<Double> contrail = null;
        if (selected != null) {
            contrail = selected::getContrailSize;
        }
        this.addValueSelector("Contrail size:", contrail,
                0, 128, 1, action, 3);
    }

    private void addStyleSelector() {
        JLabel selectorLabel = new JLabel("Engine style:");
        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), 4);
            return;
        }
        JComponent valueBox;

        // TODO: implement bulk style change.

        EngineDataOverride skinOverride = selected.getSkinOverride();

        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, EngineStyle> allEngineStyles = gameData.getAllEngineStyles();
        if (allEngineStyles != null) {
            Collection<EngineStyle> styleCollection = allEngineStyles.values();
            JComboBox<EngineStyle> styleSelector  = new JComboBox<>(styleCollection.toArray(new EngineStyle[0]));
            styleSelector.setSelectedItem(selected.getStyle());

            styleSelector.addActionListener(e -> {
                ShipPainter slotParent = (ShipPainter) selected.getParent();
                EngineSlotPainter enginePainter = slotParent.getEnginePainter();
                EngineStyle selectedValue = (EngineStyle) styleSelector.getSelectedItem();
                enginePainter.changeEngineStyleWithMirrorCheck(selected, selectedValue);
            });
            valueBox = styleSelector;

            if (skinOverride != null && skinOverride.getStyle() != null) {
                styleSelector.setToolTipText("Locked: engine style overridden by skin");
                styleSelector.setEnabled(false);
            }
        } else {
            JTextField idField = new JTextField(selected.getStyleID());
            idField.setToolTipText("Styles not loaded: defaulted to ID text");

            idField.addActionListener(e -> {
                String textValue = idField.getText();
                selected.setStyleID(textValue);
                EventBus.publish(new EnginesPanelRepaintQueued());
            });

            valueBox = idField;

            if (skinOverride != null && skinOverride.getStyleID() != null) {
                idField.setToolTipText("Locked: style overridden by skin");
                idField.setEnabled(false);
            }
        }
        ComponentUtilities.addLabelAndComponent(this, selectorLabel, valueBox, 4);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private JComponent addValueSelector(String selectorLabelText, Supplier<Double> currentValue, double minValue,
                                  double maxValue, Consumer<Double> action, int position) {
        return addValueSelector(selectorLabelText, currentValue, minValue,
                maxValue, 0.5d, action, position);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private JComponent addValueSelector(String selectorLabelText, Supplier<Double> currentValue, double minValue,
                                  double maxValue, double step, Consumer<Double> action, int position) {
        JLabel selectorLabel = new JLabel(selectorLabelText);

        String tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        selectorLabel.setToolTipText(tooltip);

        if (selected == null) {
            return ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), position);
        }
        double currentInput = currentValue.get();
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(currentInput,
                minValue, maxValue, step);
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

        return ComponentUtilities.addLabelAndComponent(this, selectorLabel, spinner, position);
    }

}
