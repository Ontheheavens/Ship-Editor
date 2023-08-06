package oth.shipeditor.components.instrument.ship;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.BoundPointsSorted;
import oth.shipeditor.components.viewer.entities.BoundPoint;

import javax.swing.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends PointList<BoundPoint> {

    BoundList(ListModel<BoundPoint> dataModel) {
        super(dataModel);
    }

    @Override
    protected void publishPointsSorted(List<BoundPoint> rearrangedPoints) {
        EventBus.publish(new BoundPointsSorted(rearrangedPoints));
    }

    @Override
    protected void handlePointSelection(BoundPoint point) {}

}
