package oth.shipeditor.components.instrument;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends JList<BoundPoint> {

    private boolean propagationBlock;

    BoundList(ListModel<BoundPoint> model) {
        super(model);
        this.addListSelectionListener(e -> {
            if (propagationBlock) {
                propagationBlock = false;
                return;
            }
            int index = this.getSelectedIndex();
            if (index != -1) {
                ListModel<BoundPoint> listModel = this.getModel();
                BoundPoint point = listModel.getElementAt(index);
                EventBus.publish(new PointSelectQueued(point));
                EventBus.publish(new ViewerRepaintQueued());
            }
        });
        this.setCellRenderer(new BoundPointCellRenderer());
        int margin = 3;
        this.setBorder(new EmptyBorder(margin, margin, margin, margin));
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked && checked.point() instanceof BoundPoint) {
                propagationBlock = true;
                this.setSelectedValue(checked.point(), true);
            }
        });
    }

    private static class BoundPointCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            WorldPoint checked = (WorldPoint) value;
            Point2D position = checked.getCoordinatesForDisplay();
            String displayText = "Bound #" + index + ": (X:" + position.getX() + ",Y:" + position.getY() + ")";
            setText(displayText);
            return this;
        }

    }

}
