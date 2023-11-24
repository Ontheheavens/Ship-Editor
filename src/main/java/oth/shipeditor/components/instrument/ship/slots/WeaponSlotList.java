package oth.shipeditor.components.instrument.ship.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.SlotPointsSorted;
import oth.shipeditor.components.datafiles.trees.WeaponFilterPanel;
import oth.shipeditor.components.instrument.ship.engines.EngineList;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.utility.components.rendering.WeaponSlotCellRenderer;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 19.11.2023
 */
public class WeaponSlotList extends PointList<WeaponSlotPoint> {

    private static final DataFlavor slotFlavor = new DataFlavor(WeaponSlotPoint.class, "Slot");

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

    @Override
    protected Transferable createTransferableFromEntry(WeaponSlotPoint entry) {
        return new Transferable() {

            private final WeaponSlotPoint slot = entry;

            private final DataFlavor sourceFlavor = new DataFlavor(WeaponSlotList.this.getClass(),
                    String.valueOf(WeaponSlotList.this.hashCode()));

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {slotFlavor, sourceFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(slotFlavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) {
                return slot;
            }
        };
    }

    @Override
    protected boolean isSupported(Transferable transferable) {
        return transferable.isDataFlavorSupported(slotFlavor);
    }

}
