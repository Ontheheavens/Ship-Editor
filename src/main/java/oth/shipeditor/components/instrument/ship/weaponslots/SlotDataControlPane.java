package oth.shipeditor.components.instrument.ship.weaponslots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Ontheheavens
 * @since 11.08.2023
 */
public class SlotDataControlPane extends JPanel {

    private static final String NO_SELECTED_SLOT = "No selected slot";
    private static final String CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS = "Change applies to all selected slots";
    private static final String CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT = "Change applies to first selected slot";
    private static final String MOUSEWHEEL_TO_CHANGE = "(Mousewheel to change)";

    private final WeaponSlotPoint selected;

    private final WeaponSlotList slotList;

    SlotDataControlPane(WeaponSlotPoint slotPoint, WeaponSlotList parent) {
        this.selected = slotPoint;
        this.slotList = parent;
        this.setLayout(new GridBagLayout());

        this.addIDPanel();

        this.addTypeSelector();
        this.addMountSelector();
        this.addSizeSelector();

        this.addAngleController();
        this.addArcController();
    }

    @SuppressWarnings("DuplicatedCode")
    private void addLabelAndComponent(JLabel label, Component component, int y) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(3, 6, 0, 3);
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.LINE_START;
        this.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.gridy = y;
        if (component instanceof JLabel) {
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(3, 3, 0, 9);
        } else {
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(3, 3, 0, 6);
        }
        constraints.anchor = GridBagConstraints.LINE_END;
        this.add(component, constraints);
    }

    private static JLabel getNoSelected() {
        JLabel label = new JLabel(NO_SELECTED_SLOT);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }

    private void addIDPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Slot ID:");
        label.setToolTipText(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT);

        Component right = SlotDataControlPane.getNoSelected();

        if (selected != null) {
            JTextField editor = new JTextField(selected.getId());
            editor.setColumns(10);
            editor.addActionListener(e -> {
                String currentText = editor.getText();
                EditDispatch.postWeaponSlotIDChanged(selected, currentText);
            });
            right = editor;
        }

        this.addLabelAndComponent(label, right, 0);
    }

    private void actOnSelectedValues(BiConsumer<WeaponSlotPainter, List<WeaponSlotPoint>> action) {
        WeaponSlotPoint selectedValue = slotList.getSelectedValue();
        if (selectedValue == null) return;
        ShipPainter parentLayer = (ShipPainter) selectedValue.getParentLayer();
        WeaponSlotPainter slotPainter = parentLayer.getWeaponSlotPainter();
        List<WeaponSlotPoint> selectedValuesList = slotList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty()) {
            action.accept(slotPainter, selectedValuesList);
            EventBus.publish(new ViewerRepaintQueued());
        }
    }

    private void addTypeSelector() {
        JLabel selectorLabel = new JLabel("Slot type:");
        selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, SlotDataControlPane.getNoSelected(), 1);
            return;
        }
        JComboBox<WeaponType> typeSelector = new JComboBox<>(WeaponType.values());

        typeSelector.removeItem(WeaponType.LAUNCH_BAY);
        typeSelector.setSelectedItem(selected.getWeaponType());

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getWeaponType() != null) {
            typeSelector.setToolTipText("Locked: slot type overridden by skin");
            typeSelector.setEnabled(false);
        } else {
            ActionListener listener = e -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) -> {
                WeaponType selectedType = (WeaponType) typeSelector.getSelectedItem();
                weaponSlotPainter.changeSlotsTypeWithMirrorCheck(selectedType, weaponSlotPoints);
            });
            typeSelector.addActionListener(listener);
        }
        this.addLabelAndComponent(selectorLabel, typeSelector, 1);
    }

    private void addMountSelector() {
        JLabel selectorLabel = new JLabel("Slot mount:");
        selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, SlotDataControlPane.getNoSelected(), 2);
            return;
        }
        JComboBox<WeaponMount> mountSelector = new JComboBox<>(WeaponMount.values());
        mountSelector.setSelectedItem(selected.getWeaponMount());

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getWeaponMount() != null) {
            mountSelector.setToolTipText("Locked: slot mount overridden by skin");
            mountSelector.setEnabled(false);
        } else {
            ActionListener listener = e -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) -> {
                WeaponMount selectedMount = (WeaponMount) mountSelector.getSelectedItem();
                weaponSlotPainter.changeSlotsMountWithMirrorCheck(selectedMount, weaponSlotPoints);
            });
            mountSelector.addActionListener(listener);
        }
        this.addLabelAndComponent(selectorLabel, mountSelector, 2);
    }

    private void addSizeSelector() {
        JLabel selectorLabel = new JLabel("Slot size:");
        selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, SlotDataControlPane.getNoSelected(), 3);
            return;
        }
        JComboBox<WeaponSize> sizeSelector = new JComboBox<>(WeaponSize.values());
        sizeSelector.setSelectedItem(selected.getWeaponSize());

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getWeaponSize() != null) {
            sizeSelector.setToolTipText("Locked: slot size overridden by skin");
            sizeSelector.setEnabled(false);
        } else {
            ActionListener listener = e -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) -> {
                WeaponSize selectedSize = (WeaponSize) sizeSelector.getSelectedItem();
                weaponSlotPainter.changeSlotsSizeWithMirrorCheck(selectedSize, weaponSlotPoints);
            });
            sizeSelector.addActionListener(listener);
        }
        this.addLabelAndComponent(selectorLabel, sizeSelector, 3);
    }

    private void addAngleController() {
        JLabel selectorLabel = new JLabel("Slot angle:");
        String tooltip = Utility.getWithLinebreaks(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, MOUSEWHEEL_TO_CHANGE);
        selectorLabel.setToolTipText(tooltip);
        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, SlotDataControlPane.getNoSelected(), 4);
            return;
        }

        double minValue = -360;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(selected.getAngle(),
                minValue, maxValue, 0.5d);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getAngle() != null) {
            spinner.setToolTipText("Locked: slot angle overridden by skin");
            spinner.setEnabled(false);
        } else {
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    Number modelNumber = spinnerNumberModel.getNumber();
                    double current = modelNumber.doubleValue();

                    ShipPainter slotParent = (ShipPainter) selected.getParentLayer();
                    WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
                    weaponSlotPainter.changeAngleWithMirrorCheck(selected, current);

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
        }

        this.addLabelAndComponent(selectorLabel, spinner, 4);
    }

    private void addArcController() {
        JLabel selectorLabel = new JLabel("Slot arc:");
        String tooltip = Utility.getWithLinebreaks(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, MOUSEWHEEL_TO_CHANGE);
        selectorLabel.setToolTipText(tooltip);
        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, SlotDataControlPane.getNoSelected(), 5);
            return;
        }

        double minValue = 0;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(selected.getArc(),
                minValue, maxValue, 1);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getArc() != null) {
            spinner.setToolTipText("Locked: slot arc overridden by skin");
            spinner.setEnabled(false);
        } else {
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    Number modelNumber = spinnerNumberModel.getNumber();
                    double current = modelNumber.doubleValue();

                    ShipPainter slotParent = (ShipPainter) selected.getParentLayer();
                    WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
                    weaponSlotPainter.changeArcWithMirrorCheck(selected, current);

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
        }

        this.addLabelAndComponent(selectorLabel, spinner, 5);
    }

}
