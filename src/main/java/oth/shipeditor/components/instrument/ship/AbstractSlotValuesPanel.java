package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;

/**
 * @author Ontheheavens
 * @since 14.08.2023
 */
public abstract class AbstractSlotValuesPanel extends JPanel {

    private static final String NO_SELECTED_SLOT = "No selected slot";

    private static final String MOUSEWHEEL_TO_CHANGE = "(Mousewheel to change)";

    private static final String CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS = "Change applies to all selected slots";
    private static final String CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT = "Change applies to first selected slot";

    @Getter
    private final SlotPoint selected;

    private final boolean multiSelectionAllowed;

    protected AbstractSlotValuesPanel(SlotPoint slotPoint, boolean multiSelection) {
        this.selected = slotPoint;
        this.multiSelectionAllowed = multiSelection;
        this.setLayout(new GridBagLayout());
        this.addContent();
    }

    protected abstract String getEntityName();

    @SuppressWarnings("WeakerAccess")
    protected void addContent() {
        this.addIDPanel();

        this.addTypeSelector();
        this.addMountSelector();
        this.addSizeSelector();

        this.addAngleController();
        this.addArcController();
    }

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

    protected abstract ActionListener getTypeSelectorListener(JComboBox<WeaponType> typeSelector);

    protected abstract ActionListener getMountSelectorListener(JComboBox<WeaponMount> mountSelector);

    protected abstract ActionListener getSizeSelectorListener(JComboBox<WeaponSize> sizeSelector);

    protected abstract ChangeListener getAngleChangeListener(JSpinner spinner,
                                                             SpinnerNumberModel spinnerNumberModel,
                                                             SlotPoint slotPoint);

    protected abstract ChangeListener getArcChangeListener(JSpinner spinner,
                                                             SpinnerNumberModel spinnerNumberModel,
                                                             SlotPoint slotPoint);

    protected void addIDPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(getEntityName() + " ID:");

        if (multiSelectionAllowed) {
            label.setToolTipText(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT);
        }

        Component right = AbstractSlotValuesPanel.getNoSelected();

        if (selected != null) {
            JTextField editor = new JTextField(selected.getId());
            editor.setColumns(10);
            editor.addActionListener(e -> {
                String currentText = editor.getText();
                EditDispatch.postSlotIDChanged(selected, currentText);
            });
            right = editor;
        }

        this.addLabelAndComponent(label, right, 0);
    }

    private void addTypeSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " type:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, AbstractSlotValuesPanel.getNoSelected(), 1);
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
            typeSelector.addActionListener(this.getTypeSelectorListener(typeSelector));
        }
        this.addLabelAndComponent(selectorLabel, typeSelector, 1);
    }

    protected void addMountSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " mount:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, AbstractSlotValuesPanel.getNoSelected(), 2);
            return;
        }
        JComboBox<WeaponMount> mountSelector = new JComboBox<>(WeaponMount.values());
        mountSelector.setSelectedItem(selected.getWeaponMount());

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getWeaponMount() != null) {
            mountSelector.setToolTipText("Locked: slot mount overridden by skin");
            mountSelector.setEnabled(false);
        } else {
            mountSelector.addActionListener(this.getMountSelectorListener(mountSelector));
        }
        this.addLabelAndComponent(selectorLabel, mountSelector, 2);
    }

    protected void addSizeSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " size:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }
        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, AbstractSlotValuesPanel.getNoSelected(), 3);
            return;
        }
        JComboBox<WeaponSize> sizeSelector = new JComboBox<>(WeaponSize.values());
        sizeSelector.setSelectedItem(selected.getWeaponSize());

        WeaponSlotOverride skinOverride = selected.getSkinOverride();
        if (skinOverride != null && skinOverride.getWeaponSize() != null) {
            sizeSelector.setToolTipText("Locked: slot size overridden by skin");
            sizeSelector.setEnabled(false);
        } else {
            sizeSelector.addActionListener(this.getSizeSelectorListener(sizeSelector));
        }
        this.addLabelAndComponent(selectorLabel, sizeSelector, 3);
    }

    protected void addAngleController() {
        JLabel selectorLabel = new JLabel(getEntityName() + " angle:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, AbstractSlotValuesPanel.getNoSelected(), 4);
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
            spinner.addChangeListener(this.getAngleChangeListener(spinner, spinnerNumberModel, selected));
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

    protected void addArcController() {
        JLabel selectorLabel = new JLabel(getEntityName() + " arc:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        if (selected == null) {
            this.addLabelAndComponent(selectorLabel, AbstractSlotValuesPanel.getNoSelected(), 5);
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
            spinner.addChangeListener(this.getArcChangeListener(spinner, spinnerNumberModel, selected));
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
