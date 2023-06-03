package oth.shipeditor.components;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundPointPanelRepaintQueued;
import oth.shipeditor.communication.events.components.ShipViewableCreated;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
@Log4j2
final class BoundList extends JList<BoundPoint> {

    private ShipViewable associatedViewer;

    private CoordsDisplayMode associatedMode = CoordsDisplayMode.WORLD;

    BoundList(ListModel<BoundPoint> model) {
        super(model);
        this.addListSelectionListener(e -> {
            int index = this.getSelectedIndex();
            if (index != -1) {
                ListModel<BoundPoint> listModel = this.getModel();
                BoundPoint point = listModel.getElementAt(index);
                point.setSelected(true);
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
            if (event instanceof CoordsModeChanged checked) {
                this.associatedMode = checked.newMode();
                EventBus.publish(new BoundPointPanelRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipViewableCreated checked) {
                this.associatedViewer = checked.viewable();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked && checked.point() instanceof BoundPoint) {
                this.setSelectedValue(checked.point(), true);
            }
        });
    }

    private class BoundPointCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            WorldPoint checked = (WorldPoint) value;
            Point2D position = getCoordinatesForDisplay(checked);
            String displayText = "Bound #" + index + ": (X:" + position.getX() + ",Y:" + position.getY() + ")";
            setText(displayText);
            return this;
        }

        private Point2D getCoordinatesForDisplay(WorldPoint input) {
            Point2D position = input.getPosition();
            Point2D result = position;
            if (BoundList.this.associatedViewer == null) {
                return result;
            }
            LayerPainter selectedLayer = BoundList.this.associatedViewer.getSelectedLayer();
            if (selectedLayer == null) {
                return result;
            }
            double positionX = position.getX();
            double positionY = position.getY();
            switch (associatedMode) {
                case WORLD -> {}
                case SCREEN -> {
                    AffineTransform worldToScreen = associatedViewer.getTransformWorldToScreen();
                    Point2D transformed =  worldToScreen.transform(result, null);
                    double resultX = transformed.getX();
                    double resultY = transformed.getY();
                    double roundedX = Math.round(resultX * 2) / 2.0;
                    double roundedY = Math.round(resultY * 2) / 2.0;
                    result = new Point2D.Double(roundedX, roundedY);
                }
                case SPRITE_CENTER -> {
                    Point2D center = selectedLayer.getSpriteCenter();
                    double centerX = center.getX();
                    double centerY = center.getY();
                    result = new Point2D.Double(positionX - centerX, positionY - centerY);
                }
                case SHIPCENTER_ANCHOR -> {
                    Point2D center = selectedLayer.getCenterAnchor();
                    double centerX = center.getX();
                    double centerY = center.getY();
                    result = new Point2D.Double(positionX - centerX, -(-positionY + centerY));
                }
                case SHIP_CENTER -> {
                    ShipCenterPoint shipCenter = selectedLayer.getShipCenter();
                    Point2D center = shipCenter.getPosition();
                    double centerX = center.getX();
                    double centerY = center.getY();
                    result = new Point2D.Double(positionX - centerX, positionY - centerY);
                }
            }
            return result;
        }
    }

}
