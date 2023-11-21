package oth.shipeditor.components.instrument.ship.engines;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.EnginePointsSorted;
import oth.shipeditor.utility.components.containers.PointList;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.utility.components.rendering.EngineCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineList extends PointList<EnginePoint> {

    private final JPanel infoPanel;

    EngineList(ListModel<EnginePoint> dataModel, JPanel infoDataPanel) {
        super(dataModel);
        this.infoPanel = infoDataPanel;
        this.setCellRenderer(new EngineCellRenderer());
    }

    @Override
    protected void handlePointSelection(EnginePoint point) {
        this.refreshEngineControlPane();
    }

    void refreshEngineControlPane() {
        EnginePoint selected = this.getSelectedValue();
        infoPanel.removeAll();

        EngineDataPanel dataPanel = new EngineDataPanel(selected);
        infoPanel.add(dataPanel, BorderLayout.CENTER);

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    @Override
    protected void publishPointsSorted(List<EnginePoint> rearrangedPoints) {
        EventBus.publish(new EnginePointsSorted(rearrangedPoints));
    }

}
