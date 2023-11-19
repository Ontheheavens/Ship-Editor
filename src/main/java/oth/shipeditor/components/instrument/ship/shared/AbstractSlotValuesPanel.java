package oth.shipeditor.components.instrument.ship.shared;

import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 14.11.2023
 */
public abstract class AbstractSlotValuesPanel extends LayerPropertiesPanel {

    private final boolean multiSelectionAllowed;

    protected AbstractSlotValuesPanel(boolean multiSelection) {
        this.multiSelectionAllowed = multiSelection;
    }

    protected abstract String getEntityName();

    protected abstract SlotData getSelectedFromLayer(LayerPainter layerPainter);

    @Override
    public ShipPainter getCachedLayerPainter() {
        return (ShipPainter) super.getCachedLayerPainter();
    }

    /**
     * @return ID from painter of cached layer that is not yet assigned to any slot.
     */
    protected abstract String getNextUniqueID();

    protected abstract Consumer<String> getIDSetter();

    protected abstract Consumer<WeaponType> getTypeSetter();

    protected abstract Consumer<WeaponMount> getMountSetter();

    protected abstract Consumer<WeaponSize> getSizeSetter();

    protected abstract Consumer<Double> getAngleSetter();

    protected abstract Consumer<Double> getArcSetter();

    protected abstract Consumer<Double> getRenderOrderSetter();

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        if (layerPainter == null || layerPainter.isUninitialized()) {
            fireClearingListeners(null);
            return;
        }

        fireRefresherListeners(layerPainter);
    }

    protected boolean shouldIncludeTypeSelector() {
        return true;
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var slotIdWidget = createIDWidget();
        widgets.put(slotIdWidget.getFirst(), slotIdWidget.getSecond());

        if (shouldIncludeTypeSelector()) {
            var slotTypeWidget = createTypeSelector();
            widgets.put(slotTypeWidget.getFirst(), slotTypeWidget.getSecond());
        }

        var slotMountWidget = createMountSelector();
        widgets.put(slotMountWidget.getFirst(), slotMountWidget.getSecond());

        var slotSizeSelector = createSizeSelector();
        widgets.put(slotSizeSelector.getFirst(), slotSizeSelector.getSecond());

        var angleController = createAngleController();
        widgets.put(angleController.getFirst(), angleController.getSecond());

        var arcController = createArcController();
        widgets.put(arcController.getFirst(), arcController.getSecond());

        var renderOrderController = createRenderOrderController();
        widgets.put(renderOrderController.getFirst(), renderOrderController.getSecond());

        JPanel widgetsPanel = createWidgetsPanel(widgets);
        this.add(widgetsPanel, BorderLayout.CENTER);
    }

    @Override
    protected void addWidgetRow(JPanel contentContainer, JLabel label, JComponent component, int ordering) {
        ComponentUtilities.addLabelAndComponent(contentContainer,
                label, component, 2, 2, 0, ordering);
    }

    private Pair<JLabel, JComponent> createIDWidget() {
        JLabel label = new JLabel(getEntityName() + " ID:");

        if (multiSelectionAllowed) {
            label.setToolTipText(StringValues.CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        JTextField editor = new JTextField();
        editor.setColumns(10);
        editor.addActionListener(e -> {
            if (isWidgetsReadyForInput()) {
                String currentText = editor.getText();
                Consumer<String> setter = getIDSetter();
                setter.accept(currentText);
            }
        });

        JPopupMenu contextMenu = getIDMenu(editor);
        String confirmHint = StringValues.ENTER_TO_SAVE_CHANGES;
        String menuHint = StringValues.RIGHT_CLICK_TO_GENERATE;
        editor.setToolTipText(Utility.getWithLinebreaks(confirmHint, menuHint));
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && editor.isEnabled()) {
                    contextMenu.show(editor, e.getX(), e.getY());
                }
            }
        });

        registerWidgetListeners(editor, layerPainter -> {
            editor.setText("");
            editor.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);
            if (selectedSlot != null) {
                editor.setText(selectedSlot.getId());
                editor.setEnabled(true);
            } else {
                editor.setText("");
                editor.setEnabled(false);
            }
        });

        return new Pair<>(label, editor);
    }

    private JPopupMenu getIDMenu(JTextField editor) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem createNextUniqueId = new JMenuItem(StringValues.CREATE_NEXT_UNIQUE_ID);
        createNextUniqueId.addActionListener(e -> {
            String nextUniqueID = getNextUniqueID();
            if (nextUniqueID != null) {
                editor.setText(nextUniqueID);
            }
        });
        contextMenu.add(createNextUniqueId);
        return contextMenu;
    }

    private Pair<JLabel, JComponent> createTypeSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " type:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(StringValues.CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        JComboBox<WeaponType> typeSelector = new JComboBox<>(WeaponType.values());
        typeSelector.removeItem(WeaponType.LAUNCH_BAY);

        typeSelector.addActionListener(e -> {
            Object selectedItem = typeSelector.getSelectedItem();
            if (isWidgetsReadyForInput() && selectedItem instanceof WeaponType weaponType) {
                Consumer<WeaponType> setter = getTypeSetter();
                setter.accept(weaponType);
            }
        });

        registerWidgetListeners(typeSelector, layerPainter -> {
            typeSelector.setSelectedItem(null);
            typeSelector.setToolTipText("");
            typeSelector.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);

            if (selectedSlot != null) {
                WeaponSlotOverride skinOverride = null;
                if (selectedSlot instanceof SlotPoint checked) {
                    skinOverride = checked.getSkinOverride();
                }

                if (skinOverride != null && skinOverride.getWeaponType() != null) {
                    typeSelector.setToolTipText("Locked: type overridden by skin");
                    typeSelector.setEnabled(false);
                } else {
                    typeSelector.setSelectedItem(selectedSlot.getWeaponType());
                    typeSelector.setEnabled(true);
                }
            } else {
                typeSelector.setSelectedItem(null);
                typeSelector.setToolTipText("");
                typeSelector.setEnabled(false);
            }
        });

        return new Pair<>(selectorLabel, typeSelector);
    }

    private Pair<JLabel, JComponent> createMountSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " mount:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(StringValues.CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        JComboBox<WeaponMount> mountSelector = new JComboBox<>(WeaponMount.values());

        mountSelector.addActionListener(e -> {
            Object selectedItem = mountSelector.getSelectedItem();
            if (isWidgetsReadyForInput() && selectedItem instanceof WeaponMount weaponMount) {
                Consumer<WeaponMount> setter = getMountSetter();
                setter.accept(weaponMount);
            }
        });

        registerWidgetListeners(mountSelector, layerPainter -> {
            mountSelector.setSelectedItem(null);
            mountSelector.setToolTipText("");
            mountSelector.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);

            if (selectedSlot != null) {
                WeaponSlotOverride skinOverride = null;
                if (selectedSlot instanceof SlotPoint checked) {
                    skinOverride = checked.getSkinOverride();
                }

                if (skinOverride != null && skinOverride.getWeaponMount() != null) {
                    mountSelector.setToolTipText("Locked: mount overridden by skin");
                    mountSelector.setEnabled(false);
                } else {
                    mountSelector.setSelectedItem(selectedSlot.getWeaponMount());
                    mountSelector.setEnabled(true);
                }
            } else {
                mountSelector.setSelectedItem(null);
                mountSelector.setToolTipText("");
                mountSelector.setEnabled(false);
            }
        });

        return new Pair<>(selectorLabel, mountSelector);
    }

    private Pair<JLabel, JComponent> createSizeSelector() {
        JLabel selectorLabel = new JLabel(getEntityName() + " size:");
        if (multiSelectionAllowed) {
            selectorLabel.setToolTipText(StringValues.CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);
        }

        JComboBox<WeaponSize> sizeSelector = new JComboBox<>(WeaponSize.values());

        sizeSelector.addActionListener(e -> {
            Object selectedItem = sizeSelector.getSelectedItem();
            if (isWidgetsReadyForInput() && selectedItem instanceof WeaponSize weaponSize) {
                Consumer<WeaponSize> setter = getSizeSetter();
                setter.accept(weaponSize);
            }
        });

        registerWidgetListeners(sizeSelector, layerPainter -> {
            sizeSelector.setSelectedItem(null);
            sizeSelector.setToolTipText("");
            sizeSelector.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);

            if (selectedSlot != null) {
                WeaponSlotOverride skinOverride = null;
                if (selectedSlot instanceof SlotPoint checked) {
                    skinOverride = checked.getSkinOverride();
                }

                if (skinOverride != null && skinOverride.getWeaponSize() != null) {
                    sizeSelector.setToolTipText("Locked: size overridden by skin");
                    sizeSelector.setEnabled(false);
                } else {
                    sizeSelector.setSelectedItem(selectedSlot.getWeaponSize());
                    sizeSelector.setEnabled(true);
                }
            } else {
                sizeSelector.setSelectedItem(null);
                sizeSelector.setToolTipText("");
                sizeSelector.setEnabled(false);
            }
        });

        return new Pair<>(selectorLabel, sizeSelector);
    }

    private Pair<JLabel, JComponent> createAngleController() {
        JLabel selectorLabel = new JLabel(getEntityName() + " angle:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(StringValues.CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, StringValues.MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        double minValue = -360;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(
                0.0d, minValue, maxValue, 0.5d
        );
        JSpinner spinner =  Spinners.createWheelable(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();

                Consumer<Double> setter = getAngleSetter();
                setter.accept(current);
            }
        });

        registerWidgetListeners(spinner, layerPainter -> {
            spinner.setValue(0.0d);
            spinner.setToolTipText("");
            spinner.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);

            if (selectedSlot != null) {
                WeaponSlotOverride skinOverride = null;
                if (selectedSlot instanceof SlotPoint checked) {
                    skinOverride = checked.getSkinOverride();
                }

                if (skinOverride != null && skinOverride.getBoxedAngle() != null) {
                    spinner.setToolTipText("Locked: angle overridden by skin");
                    spinner.setEnabled(false);
                } else {
                    spinner.setValue(selectedSlot.getAngle());
                    spinner.setEnabled(true);
                }
            } else {
                spinner.setValue(0.0d);
                spinner.setToolTipText("");
                spinner.setEnabled(false);
            }
        });

        return new Pair<>(selectorLabel, spinner);
    }

    private Pair<JLabel, JComponent> createArcController() {
        JLabel selectorLabel = new JLabel(getEntityName() + " arc:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(StringValues.CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, StringValues.MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        double minValue = 0;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(
                0.0d, minValue, maxValue, 1.0d
        );
        JSpinner spinner =  Spinners.createWheelable(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();

                Consumer<Double> setter = getArcSetter();
                setter.accept(current);
            }
        });

        registerWidgetListeners(spinner, layerPainter -> {
            spinner.setValue(0.0d);
            spinner.setToolTipText("");
            spinner.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);

            if (selectedSlot != null) {
                WeaponSlotOverride skinOverride = null;
                if (selectedSlot instanceof SlotPoint checked) {
                    skinOverride = checked.getSkinOverride();
                }

                if (skinOverride != null && skinOverride.getBoxedArc() != null) {
                    spinner.setToolTipText("Locked: arc overridden by skin");
                    spinner.setEnabled(false);
                } else {
                    spinner.setValue(selectedSlot.getArc());
                    spinner.setEnabled(true);
                }
            } else {
                spinner.setValue(0.0d);
                spinner.setToolTipText("");
                spinner.setEnabled(false);
            }
        });

        return new Pair<>(selectorLabel, spinner);
    }

    private Pair<JLabel, JComponent> createRenderOrderController() {
        JLabel selectorLabel = new JLabel("Render order:");

        String tooltip;
        if (multiSelectionAllowed) {
            tooltip = Utility.getWithLinebreaks(StringValues.CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT, StringValues.MOUSEWHEEL_TO_CHANGE);
        } else {
            tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        }
        selectorLabel.setToolTipText(tooltip);

        double minValue = Integer.MIN_VALUE + 1;
        double maxValue = Integer.MAX_VALUE - 1;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0.0d,
                minValue, maxValue, 1.0d
        );
        JSpinner spinner =  Spinners.createWheelable(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();

                Consumer<Double> setter = getRenderOrderSetter();
                setter.accept(current);
            }
        });

        registerWidgetListeners(spinner, layerPainter -> {
            spinner.setValue(0.0d);
            spinner.setToolTipText("");
            spinner.setEnabled(false);
        }, layerPainter -> {
            var selectedSlot = getSelectedFromLayer(layerPainter);

            if (selectedSlot != null) {
                WeaponSlotOverride skinOverride = null;
                if (selectedSlot instanceof SlotPoint checked) {
                    skinOverride = checked.getSkinOverride();
                }

                if (skinOverride != null && skinOverride.getRenderOrderModBoxed() != null) {
                    spinner.setToolTipText("Locked: render order overridden by skin");
                    spinner.setEnabled(false);
                } else {
                    spinner.setValue((double) selectedSlot.getRenderOrderMod());
                    spinner.setEnabled(true);
                }
            } else {
                spinner.setValue(0.0d);
                spinner.setToolTipText("");
                spinner.setEnabled(false);
            }
        });

        return new Pair<>(selectorLabel, spinner);
    }

}
