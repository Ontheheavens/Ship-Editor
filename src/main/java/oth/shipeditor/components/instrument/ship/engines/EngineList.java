package oth.shipeditor.components.instrument.ship.engines;

import com.formdev.flatlaf.ui.FlatLineBorder;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.EnginePointsSorted;
import oth.shipeditor.components.instrument.ship.PointList;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.rendering.PointCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    private static class EngineCellRenderer extends PointCellRenderer {

        private final JLabel styleIcon;

        EngineCellRenderer() {
            styleIcon = new JLabel();
            styleIcon.setOpaque(true);
            styleIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
            styleIcon.setBackground(Color.LIGHT_GRAY);

            JPanel leftContainer = getLeftContainer();
            leftContainer.removeAll();
            leftContainer.add(styleIcon);
            JLabel textLabel = getTextLabel();
            textLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
            leftContainer.add(textLabel);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends BaseWorldPoint> list,
                                                      BaseWorldPoint value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            EnginePoint checked = (EnginePoint) value;
            EngineStyle engineStyle = checked.getStyle();
            String styleOrID = checked.getStyleID();
            styleIcon.setIcon(null);
            styleIcon.setVisible(false);
            if (engineStyle != null) {
                styleOrID = engineStyle.getEngineStyleID();

                Icon color = ComponentUtilities.createIconFromColor(engineStyle.getEngineColor(), 10, 10);

                styleIcon.setIcon(color);
                styleIcon.setVisible(true);
            }
            String displayText = styleOrID + " #" + index + ":";

            JLabel textLabel = getTextLabel();
            textLabel.setText(displayText);

            return this;
        }

    }

}
