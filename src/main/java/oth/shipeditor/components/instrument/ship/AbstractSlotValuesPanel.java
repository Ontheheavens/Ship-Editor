package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Ontheheavens
 * @since 14.08.2023
 */
public abstract class AbstractSlotValuesPanel extends JPanel {

    private static final String CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS = "Change applies to all selected slots";
    private static final String CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT = "Change applies to first selected slot";

    @Getter
    private final SlotData selected;

    private final boolean multiSelectionAllowed;

    protected AbstractSlotValuesPanel(SlotData slot, boolean multiSelection) {
        this.selected = slot;
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

    protected abstract String getNextUniqueID(ShipPainter shipPainter);

    protected abstract ActionListener getTypeSelectorListener(JComboBox<WeaponType> typeSelector);

    protected abstract ActionListener getMountSelectorListener(JComboBox<WeaponMount> mountSelector);

    protected abstract ActionListener getSizeSelectorListener(JComboBox<WeaponSize> sizeSelector);

    protected abstract ChangeListener getAngleChangeListener(JSpinner spinner,
                                                             SpinnerNumberModel spinnerNumberModel,
                                                             SlotData slotPoint);

    protected abstract ChangeListener getArcChangeListener(JSpinner spinner,
                                                             SpinnerNumberModel spinnerNumberModel,
                                                           SlotData slotPoint);

    protected void addIDPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(getEntityName() + " ID:");

        if (multiSelectionAllowed) {
            label.setToolTipText(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT);
        }

        JComponent right = ComponentUtilities.getNoSelected();

        if (selected != null) {
            JTextField editor = new JTextField(selected.getId());
            editor.setColumns(10);
            editor.addActionListener(e -> {
                String currentText = editor.getText();
                EditDispatch.postSlotIDChanged(selected, currentText);
            });
            right = editor;

            JPopupMenu contextMenu = getIDMenu(editor);
            editor.setToolTipText("Right-click to generate");
            editor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        contextMenu.show(editor, e.getX(), e.getY());
                    }
                }
            });
        }

        ComponentUtilities.addLabelAndComponent(this, label, right, 0);
    }

    private JPopupMenu getIDMenu(JTextField editor) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem createNextUniqueId = new JMenuItem("Create next unique ID");
        createNextUniqueId.addActionListener(e -> {
            var layer = StaticController.getActiveLayer();
            if (!(layer instanceof ShipLayer shipLayer)) return;
            var shipPainter = shipLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return;

            editor.setText(getNextUniqueID(shipPainter));
        });
        contextMenu.add(createNextUniqueId);
        return contextMenu;
    }

    private void addTypeSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " type:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), 1);
            return;
        }
        JComboBox<WeaponType> typeSelector = new JComboBox<>(WeaponType.values());

        typeSelector.removeItem(WeaponType.LAUNCH_BAY);
        typeSelector.setSelectedItem(selected.getWeaponType());

        WeaponSlotOverride skinOverride = null;
        if (selected instanceof SlotPoint checked) {
            skinOverride = checked.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getWeaponType() != null) {
            typeSelector.setToolTipText("Locked: slot type overridden by skin");
            typeSelector.setEnabled(false);
        } else {
            typeSelector.addActionListener(this.getTypeSelectorListener(typeSelector));
        }
        ComponentUtilities.addLabelAndComponent(this, selectorLabel, typeSelector, 1);
    }

    protected void addMountSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " mount:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), 2);
            return;
        }
        JComboBox<WeaponMount> mountSelector = new JComboBox<>(WeaponMount.values());
        mountSelector.setSelectedItem(selected.getWeaponMount());

        WeaponSlotOverride skinOverride = null;
        if (selected instanceof SlotPoint checked) {
            skinOverride = checked.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getWeaponMount() != null) {
            mountSelector.setToolTipText("Locked: slot mount overridden by skin");
            mountSelector.setEnabled(false);
        } else {
            mountSelector.addActionListener(this.getMountSelectorListener(mountSelector));
        }
        ComponentUtilities.addLabelAndComponent(this, selectorLabel, mountSelector, 2);
    }

    protected void addSizeSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " size:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }
        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), 3);
            return;
        }
        JComboBox<WeaponSize> sizeSelector = new JComboBox<>(WeaponSize.values());
        sizeSelector.setSelectedItem(selected.getWeaponSize());

        WeaponSlotOverride skinOverride = null;
        if (selected instanceof SlotPoint checked) {
            skinOverride = checked.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getWeaponSize() != null) {
            sizeSelector.setToolTipText("Locked: slot size overridden by skin");
            sizeSelector.setEnabled(false);
        } else {
            sizeSelector.addActionListener(this.getSizeSelectorListener(sizeSelector));
        }
        ComponentUtilities.addLabelAndComponent(this, selectorLabel, sizeSelector, 3);
    }

    protected void addAngleController() {
        JLabel selectorLabel = new JLabel(getEntityName() + " angle:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, StringValues.MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), 4);
            return;
        }
        double minValue = -360;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(selected.getAngle(),
                minValue, maxValue, 0.5d);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        WeaponSlotOverride skinOverride = null;
        if (selected instanceof SlotPoint checked) {
            skinOverride = checked.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getBoxedAngle() != null) {
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

        ComponentUtilities.addLabelAndComponent(this, selectorLabel, spinner, 4);
    }

    protected void addArcController() {
        JLabel selectorLabel = new JLabel(getEntityName() + " arc:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, StringValues.MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        if (selected == null) {
            ComponentUtilities.addLabelAndComponent(this, selectorLabel, ComponentUtilities.getNoSelected(), 5);
            return;
        }

        double minValue = 0;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(selected.getArc(),
                minValue, maxValue, 1);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        WeaponSlotOverride skinOverride = null;
        if (selected instanceof SlotPoint checked) {
            skinOverride = checked.getSkinOverride();
        }

        if (skinOverride != null && skinOverride.getBoxedArc() != null) {
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

        ComponentUtilities.addLabelAndComponent(this, selectorLabel, spinner, 5);
    }

}
