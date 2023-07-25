package oth.shipeditor.components.instrument;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends JList<BoundPoint> {

    BoundList(ListModel<BoundPoint> model) {
        super(model);

        MouseListener selectionListener = new ListSelectionListener();
        this.addMouseListener(selectionListener);

        this.setCellRenderer(new BoundPointCellRenderer());
        int margin = 3;
        this.setBorder(new EmptyBorder(margin, margin, margin, margin));
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked && checked.point() instanceof BoundPoint) {
                this.setSelectedValue(checked.point(), true);
            }
        });
    }

    private static class BoundPointCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            BaseWorldPoint checked = (BaseWorldPoint) value;
            String displayText = "Bound #" + index + ": " + checked.getPositionText();
            setText(displayText);
            return this;
        }

    }

    private class ListSelectionListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                if (getSelectedIndex() != -1) {
                    int index = locationToIndex(e.getPoint());
                    ListModel<BoundPoint> listModel = getModel();
                    BoundPoint point = listModel.getElementAt(index);
                    EventBus.publish(new PointSelectQueued(point));
                    EventBus.publish(new ViewerRepaintQueued());
                }
            }
        }

    }

}
