package oth.shipeditor.components.instrument.ship.slots;

import lombok.Getter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotCreationPane extends JPanel {

    @Getter
    private static WeaponType defaultType = WeaponType.BALLISTIC;

    @Getter
    private static WeaponMount defaultMount = WeaponMount.TURRET;

    @Getter
    private static WeaponSize defaultSize = WeaponSize.SMALL;

    @Getter
    private static double defaultAngle;

    @Getter
    private static double defaultArc;

    @Getter
    private static SlotCreationMode mode = SlotCreationMode.BY_DEFAULT;

    SlotCreationPane() {
        this.setLayout(new BorderLayout());

        this.add(SlotCreationPane.createModePanel(), BorderLayout.PAGE_START);

        JPanel selectorsPane = new JPanel();
        selectorsPane.setLayout(new BoxLayout(selectorsPane, BoxLayout.PAGE_AXIS));
        selectorsPane.setAlignmentY(0);
        selectorsPane.add(Box.createRigidArea(new Dimension(10, 4)));
        selectorsPane.add(SlotCreationPane.createDefaultValueSpinners());
        selectorsPane.add(Box.createRigidArea(new Dimension(10, 4)));
        selectorsPane.add(SlotCreationPane.createSlotTypeSelectors());
        selectorsPane.add(SlotCreationPane.createSlotMountSelectors());
        selectorsPane.add(SlotCreationPane.createSlotSizeSelectors());
        selectorsPane.add(Box.createVerticalGlue());

        JScrollPane scrollContainer = new JScrollPane(selectorsPane);

        this.add(scrollContainer, BorderLayout.CENTER);
        this.setPreferredSize(this.getMinimumSize());
    }

    private static JPanel createModePanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        MatteBorder matteLine = new MatteBorder(new Insets(1, 0, 0, 0),
                Color.LIGHT_GRAY);
        Border titledBorder = new TitledBorder(matteLine, "New slot values",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION);
        container.setBorder(titledBorder);

        ButtonGroup selectorButtons = new ButtonGroup();

        JRadioButton fromClosestSlot = new JRadioButton("From closest slot");
        fromClosestSlot.addActionListener(e -> mode = SlotCreationMode.BY_CLOSEST);
        container.add(SlotCreationPane.createSlotKindPane(selectorButtons, fromClosestSlot));

        JRadioButton fromPanelDefaults = new JRadioButton("From panel defaults");
        fromPanelDefaults.addActionListener(e -> mode = SlotCreationMode.BY_DEFAULT);
        container.add(SlotCreationPane.createSlotKindPane(selectorButtons, fromPanelDefaults));
        fromPanelDefaults.setSelected(true);

        return container;
    }

    private static void addLabelWithSpinner(JPanel container, String labelText, Consumer<Double> spinnerEffect, int y) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(3, 10, 0, 3);
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
        constraints.insets = new Insets(3, 3, 0, 6);
        constraints.anchor = GridBagConstraints.LINE_END;

        double minValue = 0;
        double maxValue = 360;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0,
                minValue, maxValue, 0.5d);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            Number modelNumber = spinnerNumberModel.getNumber();
            double current = modelNumber.doubleValue();
            spinnerEffect.accept(current);
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

        container.add(spinner, constraints);
    }

    private static JPanel createDefaultValueSpinners() {
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());

        SlotCreationPane.addLabelWithSpinner(container, "Default angle:",
                aDouble -> defaultAngle = aDouble, 0);

        SlotCreationPane.addLabelWithSpinner(container, "Default arc:",
                aDouble -> defaultArc = aDouble, 1);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setAlignmentX(0.5f);
        wrapper.setAlignmentY(0);
        Dimension containerPreferredSize = container.getPreferredSize();
        wrapper.setMaximumSize(new Dimension(container.getMaximumSize().width, containerPreferredSize.height));
        wrapper.add(container, BorderLayout.CENTER);
        return wrapper;
    }

    private static JPanel createSlotTypeSelectors() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Default type");

        Collection<WeaponType> types = new ArrayList<>(List.of(WeaponType.values()));
        types.remove(WeaponType.LAUNCH_BAY);

        ButtonGroup selectorButtons = new ButtonGroup();

        for (WeaponType type : types) {
            Pair<JPanel, JRadioButton> containedButton = SlotCreationPane.createSlotTypeButton(type);
            container.add(containedButton.getFirst());
            JRadioButton radioButton = containedButton.getSecond();
            selectorButtons.add(radioButton);

            if (type == defaultType) {
                radioButton.setSelected(true);
            }
        }
        container.add(Box.createRigidArea(new Dimension(10, 4)));
        return container;
    }

    private static JPanel createSlotMountSelectors() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Default mount");

        Iterable<WeaponMount> mounts = new ArrayList<>(List.of(WeaponMount.values()));

        ButtonGroup selectorButtons = new ButtonGroup();

        for (WeaponMount mount : mounts) {
            JRadioButton button = new JRadioButton(mount.getDisplayName());
            button.addActionListener(e -> defaultMount = mount);

            container.add(SlotCreationPane.createSlotKindPane(selectorButtons, button));

            if (mount == defaultMount) {
                button.setSelected(true);
            }
        }

        return container;
    }

    private static JPanel createSlotSizeSelectors() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Default size");

        Iterable<WeaponSize> sizes = new ArrayList<>(List.of(WeaponSize.values()));

        ButtonGroup selectorButtons = new ButtonGroup();

        for (WeaponSize size : sizes) {
            JRadioButton button = new JRadioButton(size.getDisplayName());
            button.addActionListener(e -> defaultSize = size);

            container.add(SlotCreationPane.createSlotKindPane(selectorButtons, button));

            if (size == defaultSize) {
                button.setSelected(true);
            }
        }

        return container;
    }

    private static JPanel createSlotKindPane(ButtonGroup selectorButtons, JRadioButton button) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(new EmptyBorder(4, 4, 0, 6));

        ComponentUtilities.layoutAsOpposites(buttonPanel, button, new JLabel(""), 0);

        selectorButtons.add(button);
        return buttonPanel;
    }

    private static Pair<JPanel, JRadioButton> createSlotTypeButton(WeaponType type) {
        JRadioButton button = new JRadioButton(type.getDisplayName());
        button.addActionListener(e -> defaultType = type);

        JPanel panel = ComponentUtilities.createColorPropertyPanel(button, type.getColor());
        panel.setBorder(new EmptyBorder(4, 4, 0, 6));
        return new Pair<>(panel, button);
    }

}
