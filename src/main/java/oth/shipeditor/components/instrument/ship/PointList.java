package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointRemoveQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
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
 * @since 29.07.2023
 */
public abstract class PointList<T extends BaseWorldPoint> extends SortableList<T> {

    private boolean propagationBlock;

    protected PointList(ListModel<T> dataModel) {
        super(dataModel);
        this.addListSelectionListener(e -> {
            if (propagationBlock) {
                propagationBlock = false;
                return;
            }
            this.actOnSelectedPoint(point -> {
                EventBus.publish(new PointSelectQueued(point));
                EventBus.publish(new ViewerRepaintQueued());
            });
        });
        this.addMouseListener(new ListContextMenuListener());
        this.setCellRenderer(new PointCellRenderer());
        int margin = 3;
        this.setBorder(new EmptyBorder(margin, margin, margin, margin));
        this.initListeners();
    }

    @Override
    protected void sortListModel() {
        ListModel<T> model = this.getModel();
        List<T> rearrangedPoints = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            T point = model.getElementAt(i);
            rearrangedPoints.add(point);
        }
        this.publishPointsSorted(rearrangedPoints);
    }

    protected abstract void publishPointsSorted(List<T> rearrangedPoints);

    private void actOnSelectedPoint(Consumer<T> action) {
        int index = this.getSelectedIndex();
        if (index != -1) {
            ListModel<T> listModel = this.getModel();
            T point = listModel.getElementAt(index);
            action.accept(point);
        }
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                DefaultListModel<T> model = (DefaultListModel<T>) this.getModel();
                if (!model.contains(checked.point())) return;
                propagationBlock = true;
                this.setSelectedValue(checked.point(), true);
            }
        });
    }

    private static class PointCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            BaseWorldPoint checked = (BaseWorldPoint) value;
            String displayText = checked.getNameForLabel() + " #" + index + ": " + checked.getPositionText();
            setText(displayText);
            return this;
        }
    }

    private class ListContextMenuListener extends MouseAdapter {

        private JPopupMenu getContextMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem removePoint = new JMenuItem("Remove point");
            removePoint.addActionListener(event -> actOnSelectedPoint(point ->
                    EventBus.publish(new PointRemoveQueued(point, true))));
            menu.add(removePoint);
            menu.addSeparator();
            JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
            adjustPosition.addActionListener(event ->
                    actOnSelectedPoint(DialogUtilities::showAdjustPointDialog));
            menu.add(adjustPosition);
            return menu;
        }

        public void mousePressed(MouseEvent e) {
            if ( SwingUtilities.isRightMouseButton(e) ) {
                setSelectedIndex(locationToIndex(e.getPoint()));
                JPopupMenu menu = getContextMenu();
                menu.show(PointList.this, e.getPoint().x, e.getPoint().y);
            }
        }

    }

}