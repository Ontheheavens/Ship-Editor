package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.CenterPointPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 10.11.2023
 */
class ShipCenterLocationWidget extends PointLocationWidget {

    private final CollisionPanel parentPanel;

    ShipCenterLocationWidget(CollisionPanel parent) {
        this.parentPanel = parent;
    }

    @Override
    protected void populateContent() {
        super.populateContent();
        String name = "Center position:";
        var dependentCoordsPanel = createDependentCoordinatesLabel(name);
        this.add(dependentCoordsPanel, BorderLayout.PAGE_START);
    }

    @Override
    protected void addWidgetRow(JPanel contentContainer, JLabel label, JComponent component, int ordering) {
        ComponentUtilities.addLabelAndComponent(contentContainer,
                label, component, 3, 5, 0, ordering);
    }


    @Override
    protected boolean isLayerPainterEligible(LayerPainter layerPainter) {
        return layerPainter instanceof ShipPainter shipPainter && !shipPainter.isUninitialized();
    }

    @Override
    protected String getPanelTitleText() {
        return StringValues.SHIP_CENTER;
    }

    @Override
    protected Supplier<Point2D> retrieveGetter() {
        return () -> {
            LayerPainter cachedLayerPainter = parentPanel.getCachedLayerPainter();
            if (isLayerPainterEligible(cachedLayerPainter)) {
                CenterPointPainter centerPointPainter = ((ShipPainter) cachedLayerPainter).getCenterPointPainter();
                ShipCenterPoint shipCenterPoint = centerPointPainter.getCenterPoint();
                return shipCenterPoint.getPosition();
            }
            return null;
        };
    }

    @Override
    protected Consumer<Point2D> retrieveSetter() {
        return point -> {
            LayerPainter cachedLayerPainter = parentPanel.getCachedLayerPainter();
            if (isLayerPainterEligible(cachedLayerPainter)) {
                CenterPointPainter centerPointPainter = ((ShipPainter) cachedLayerPainter).getCenterPointPainter();
                ShipCenterPoint shipCenterPoint = centerPointPainter.getCenterPoint();
                EditDispatch.postPointDragged(shipCenterPoint, point);
                EditDispatch.notifyTimedEditCommenced();
            }
        };
    }

}
