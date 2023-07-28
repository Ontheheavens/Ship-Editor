package oth.shipeditor.components.instrument.ship;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.BoundPointsSorted;
import oth.shipeditor.communication.events.viewer.points.PointRemoveQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.utility.components.SortableList;
import oth.shipeditor.utility.components.dialog.DialogUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends SortableList<BoundPoint> {

    private boolean propagationBlock;

    BoundList(ListModel<BoundPoint> model) {
        super(model);
        this.addListSelectionListener(e -> {
            if (propagationBlock) {
                propagationBlock = false;
                return;
            }
            this.actOnSelectedBound(boundPoint -> {
                EventBus.publish(new PointSelectQueued(boundPoint));
                EventBus.publish(new ViewerRepaintQueued());
            });
        });
        this.addMouseListener(new ListContextMenuListener());
        this.setCellRenderer(new BoundPointCellRenderer());
        int margin = 3;
        this.setBorder(new EmptyBorder(margin, margin, margin, margin));
        this.initListeners();
    }

    private void actOnSelectedBound(Consumer<BoundPoint> action) {
        int index = this.getSelectedIndex();
        if (index != -1) {
            ListModel<BoundPoint> listModel = this.getModel();
            BoundPoint point = listModel.getElementAt(index);
            action.accept(point);
        }
    }

    @Override
    protected void sortListModel() {
        ListModel<BoundPoint> model = this.getModel();
        List<BoundPoint> rearrangedBounds = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            BoundPoint boundPoint = model.getElementAt(i);
            rearrangedBounds.add(boundPoint);
        }
        EventBus.publish(new BoundPointsSorted(rearrangedBounds));
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
            BaseWorldPoint checked = (BaseWorldPoint) value;
            String displayText = "Bound #" + index + ": " + checked.getPositionText();
            setText(displayText);
            return this;
        }

    }

    private class ListContextMenuListener extends MouseAdapter {

        private JPopupMenu getContextMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem removeBound = new JMenuItem("Remove bound");
            removeBound.addActionListener(event -> actOnSelectedBound(boundPoint ->
                    EventBus.publish(new PointRemoveQueued(boundPoint, true))));
            menu.add(removeBound);
            menu.addSeparator();
            JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
            adjustPosition.addActionListener(event ->
                    actOnSelectedBound(DialogUtilities::showAdjustPointDialog));
            menu.add(adjustPosition);
            return menu;
        }

        public void mousePressed(MouseEvent e) {
            if ( SwingUtilities.isRightMouseButton(e) ) {
                setSelectedIndex(locationToIndex(e.getPoint()));
                JPopupMenu menu = getContextMenu();
                menu.show(BoundList.this, e.getPoint().x, e.getPoint().y);
            }
        }

    }


}
