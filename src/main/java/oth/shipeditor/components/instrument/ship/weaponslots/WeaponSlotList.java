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

    @SuppressWarnings("FieldCanBeLocal")
    private SlotDataControlPane slotControlPane;

    WeaponSlotList(ListModel<WeaponSlotPoint> dataModel, JPanel infoPane) {
        super(dataModel);
        this.infoPanel = infoPane;
        this.setCellRenderer(new SlotCellRenderer());
    }

    @Override
    protected void handlePointSelection(WeaponSlotPoint point) {
        refreshSlotControlPane();
    }

    public void refreshSlotControlPane() {
        WeaponSlotPoint selected = this.getSelectedValue();
        infoPanel.removeAll();

        slotControlPane = new SlotDataControlPane(selected, this);
        infoPanel.add(slotControlPane, BorderLayout.CENTER);

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
