package oth.shipeditor.components.instrument.ship.bounds;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.BoundPointsSorted;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.components.viewer.entities.BoundPoint;

import javax.swing.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends PointList<BoundPoint> {

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

}
