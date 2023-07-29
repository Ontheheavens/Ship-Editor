package oth.shipeditor.components.instrument.ship.slots;

import oth.shipeditor.components.instrument.ship.PointList;
import oth.shipeditor.components.viewer.entities.WeaponSlotPoint;

import javax.swing.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class SlotList extends PointList<WeaponSlotPoint> {

    SlotList(ListModel<WeaponSlotPoint> dataModel) {
        super(dataModel);
    }

    @Override
    protected void publishPointsSorted(List<WeaponSlotPoint> rearrangedPoints) {}

}
