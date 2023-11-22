package oth.shipeditor.components.instrument.ship.engines;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.EnginePointsSorted;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.utility.components.rendering.EngineCellRenderer;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 21.11.2023
 */
public class EngineList extends PointList<EnginePoint> {

    private final Consumer<EnginePoint> selectAction;

    EngineList(ListModel<EnginePoint> dataModel, Consumer<EnginePoint> pointSelectAction) {
        super(dataModel);
        this.setCellRenderer(new EngineCellRenderer());
        this.selectAction = pointSelectAction;
    }

    @Override
    protected void handlePointSelection(EnginePoint point) {
        this.selectAction.accept(point);
    }

    @Override
    protected void publishPointsSorted(List<EnginePoint> rearrangedPoints) {
        EventBus.publish(new EnginePointsSorted(rearrangedPoints));
    }

}
