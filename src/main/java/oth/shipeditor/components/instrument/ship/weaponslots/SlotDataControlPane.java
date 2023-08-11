package oth.shipeditor.components.instrument.ship.weaponslots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 11.08.2023
 */
public class SlotDataControlPane extends JPanel {

    private final WeaponSlotPoint selected;

    private final WeaponSlotList slotList;

    SlotDataControlPane(WeaponSlotPoint slotPoint, WeaponSlotList parent) {
        this.selected = slotPoint;
        this.slotList = parent;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(this.createIDPanel());
        this.add(this.createTypeSelector());
        this.add(new JLabel("Mount: " + selected.getWeaponMount().getDisplayName()));
        this.add(new JLabel("Size: " + selected.getWeaponSize().getDisplayName()));
        this.add(new JLabel("Angle: " + selected.getAngle()));
        this.add(new JLabel("Arc: " + selected.getArc()));
    }

    private JPanel createIDPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Slot ID:");

        JTextField editor = new JTextField(selected.getId());

        editor.setMaximumSize(editor.getPreferredSize());

        editor.setColumns(10);
        editor.addActionListener(e -> {
            String text = editor.getText();
            // TODO: add slot ID validation.
            selected.setId(text);
            slotList.repaint();
        });

        ComponentUtilities.layoutAsOpposites(container, label, editor, 6);
        return container;
    }

    private JPanel createTypeSelector() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Slot type:");

        JComboBox<WeaponType> typeSelector = new JComboBox<>(WeaponType.values());
        typeSelector.setMaximumSize(typeSelector.getPreferredSize());
        typeSelector.removeItem(WeaponType.LAUNCH_BAY);
        typeSelector.setSelectedItem(selected.getWeaponType());
        typeSelector.addActionListener(e -> {
            WeaponType selectedType = (WeaponType) typeSelector.getSelectedItem();
            for (WeaponSlotPoint slot : slotList.getSelectedValuesList()) {
                slot.setWeaponType(selectedType);
            }
            EventBus.publish(new ViewerRepaintQueued());
        });

        ComponentUtilities.layoutAsOpposites(container, label, typeSelector, 6);
        return container;
    }

}
