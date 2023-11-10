package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.ShieldPointPainter;
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
class ShieldCenterLocationWidget extends PointLocationWidget {

    private final ShieldPanel shieldPanel;

    ShieldCenterLocationWidget(ShieldPanel parent) {
        this.shieldPanel = parent;
    }

    @Override
    protected void populateContent() {
        super.populateContent();
        String name = StringValues.SHIELD_POSITION;
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
        return StringValues.SHIELD_CENTER;
    }

    @Override
    protected Supplier<Point2D> retrieveGetter() {
        return () -> {
            LayerPainter cachedLayerPainter = shieldPanel.getCachedLayerPainter();
            if (isLayerPainterEligible(cachedLayerPainter)) {
                ShieldPointPainter shieldPointPainter = ((ShipPainter) cachedLayerPainter).getShieldPointPainter();
                ShieldCenterPoint shieldCenterPoint = shieldPointPainter.getShieldCenterPoint();

                return shieldCenterPoint.getPosition();
            }
            return null;
        };
    }

    @Override
    protected Consumer<Point2D> retrieveSetter() {
        return point -> {
            LayerPainter cachedLayerPainter = shieldPanel.getCachedLayerPainter();
            if (isLayerPainterEligible(cachedLayerPainter)) {
                ShieldPointPainter shieldPointPainter = ((ShipPainter) cachedLayerPainter).getShieldPointPainter();
                ShieldCenterPoint shieldCenterPoint = shieldPointPainter.getShieldCenterPoint();
                EditDispatch.postPointDragged(shieldCenterPoint, point);
                EditDispatch.notifyTimedEditCommenced();
            }
        };
    }

}
