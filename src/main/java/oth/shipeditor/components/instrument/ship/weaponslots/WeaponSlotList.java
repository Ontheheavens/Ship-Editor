package oth.shipeditor.components.instrument.ship.weaponslots;

import oth.shipeditor.components.instrument.ship.PointList;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class WeaponSlotList extends PointList<WeaponSlotPoint> {

    private final JPanel infoPanel;

    WeaponSlotList(ListModel<WeaponSlotPoint> dataModel, JPanel panel) {
        super(dataModel);
        this.infoPanel = panel;
        this.setCellRenderer(new SlotCellRenderer());
    }

    @Override
    protected void handlePointSelection(WeaponSlotPoint point) {
        refreshSelectedInfo();
    }

    void refreshSelectedInfo() {
        WeaponSlotPoint selected = this.getSelectedValue();
        if (selected == null) return;
        infoPanel.removeAll();
        // TODO: sort out later.
        infoPanel.add(new JLabel("Type: " + selected.getWeaponType().getDisplayName()));
        infoPanel.add(new JLabel("Mount: " + selected.getWeaponMount().getDisplayName()));
        infoPanel.add(new JLabel("Size: " + selected.getWeaponSize().getDisplayName()));
        infoPanel.add(new JLabel("Angle: " + selected.getAngle()));
        infoPanel.add(new JLabel("Arc: " + selected.getArc()));
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    @Override
    protected void publishPointsSorted(List<WeaponSlotPoint> rearrangedPoints) {}

    private static class SlotCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            WeaponSlotPoint checked = (WeaponSlotPoint) value;
            String displayText = checked.getId() +": " + checked.getPositionText();
            setText(displayText);
            return this;
        }
    }

}
