package oth.shipeditor.components.instrument.ship.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.SlotPointsSorted;
import oth.shipeditor.components.datafiles.trees.WeaponFilterPanel;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.utility.components.rendering.WeaponSlotCellRenderer;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 19.11.2023
 */
public class WeaponSlotList extends PointList<WeaponSlotPoint> {

    private final Consumer<WeaponSlotPoint> selectAction;

    WeaponSlotList(ListModel<WeaponSlotPoint> dataModel, Consumer<WeaponSlotPoint> pointSelectAction) {
        super(dataModel);
        this.setCellRenderer(new WeaponSlotCellRenderer());
        this.selectAction = pointSelectAction;
    }

    @Override
    protected void handlePointSelection(WeaponSlotPoint point) {
        WeaponFilterPanel.setLastSelectedSlot(point);
        this.selectAction.accept(point);
    }

    @Override
    protected void publishPointsSorted(List<WeaponSlotPoint> rearrangedPoints) {
        EventBus.publish(new SlotPointsSorted(rearrangedPoints));
    }

}
