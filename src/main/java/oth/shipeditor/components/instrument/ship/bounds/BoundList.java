package oth.shipeditor.components.instrument.ship.bounds;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.BoundPointsSorted;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends PointList<BoundPoint> {

    private static final DataFlavor boundFlavor = new DataFlavor(BoundPoint.class, StringValues.BOUND);

    private final Runnable selectionRefresher;

    BoundList(ListModel<BoundPoint> dataModel, Runnable refresher) {
        super(dataModel);
        this.selectionRefresher = refresher;
    }

    @Override
    protected void publishPointsSorted(List<BoundPoint> rearrangedPoints) {
        EventBus.publish(new BoundPointsSorted(rearrangedPoints));
    }

    @Override
    protected void handlePointSelection(BoundPoint point) {
        if (this.selectionRefresher != null) {
            selectionRefresher.run();
        }
    }

    @Override
    protected Transferable createTransferableFromEntry(BoundPoint entry) {
        return new Transferable() {

            private final BoundPoint bound = entry;

            private final DataFlavor sourceFlavor = new DataFlavor(BoundList.this.getClass(),
                    String.valueOf(BoundList.this.hashCode()));

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {boundFlavor, sourceFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(boundFlavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) {
                return bound;
            }
        };
    }

    @Override
    protected boolean isSupported(Transferable transferable) {
        return transferable.isDataFlavorSupported(boundFlavor);
    }

}
