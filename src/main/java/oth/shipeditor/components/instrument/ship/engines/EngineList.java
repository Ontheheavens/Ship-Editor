package oth.shipeditor.components.instrument.ship.engines;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.EnginePointsSorted;
import oth.shipeditor.components.instrument.ship.PointList;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.representation.EngineStyle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineList extends PointList<EnginePoint> {

    private final JPanel infoPanel;

    private EngineDataPanel dataPanel;

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

        dataPanel = new EngineDataPanel(selected);
        infoPanel.add(dataPanel, BorderLayout.CENTER);

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    @Override
    protected void publishPointsSorted(List<EnginePoint> rearrangedPoints) {
        EventBus.publish(new EnginePointsSorted(rearrangedPoints));
    }

    private static class EngineCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            EnginePoint checked = (EnginePoint) value;
            EngineStyle engineStyle = checked.getStyle();
            String engineOrStyle = "Engine";
            if (engineStyle != null) {
                engineOrStyle = engineStyle.getEngineStyleID();
            }
            String displayText = engineOrStyle + " #" + index + ": " + checked.getPositionText();
            setText(displayText);
            return this;
        }
    }

}
