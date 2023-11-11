package oth.shipeditor.components.instrument.ship.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.SlotPointsSorted;
import oth.shipeditor.components.datafiles.trees.WeaponFilterPanel;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.utility.components.rendering.WeaponSlotCellRenderer;

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
        this.setCellRenderer(new WeaponSlotCellRenderer());
    }

    @Override
    protected void handlePointSelection(WeaponSlotPoint point) {
        refreshSlotControlPane();
    }

    void refreshSlotControlPane() {
        WeaponSlotPoint selected = this.getSelectedValue();

        WeaponFilterPanel.setLastSelectedSlot(selected);

        infoPanel.removeAll();

        slotControlPane = new SlotDataControlPane(selected, this);
        infoPanel.add(slotControlPane, BorderLayout.CENTER);

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    @Override
    protected void publishPointsSorted(List<WeaponSlotPoint> rearrangedPoints) {
        EventBus.publish(new SlotPointsSorted(rearrangedPoints));
    }

}
