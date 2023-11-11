package oth.shipeditor.components.instrument.ship.bounds;

import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.BoundPointsPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 11.11.2023
 */
class BoundLocationWidget extends PointLocationWidget {

    private final BoundsPanel parentPanel;

    BoundLocationWidget(BoundsPanel parent) {
        this.parentPanel = parent;
    }

    @Override
    protected void populateContent() {
        super.populateContent();
        String name = "Bound position:";
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
        return "Selected Bound";
    }

    @Override
    protected Supplier<Point2D> retrieveGetter() {
        return () -> {
            LayerPainter cachedLayerPainter = parentPanel.getCachedLayerPainter();
            if (isLayerPainterEligible(cachedLayerPainter)) {
                BoundPointsPainter boundsPainter = ((ShipPainter) cachedLayerPainter).getBoundsPainter();
                BoundPoint selected = boundsPainter.getSelected();
                if (selected != null) {
                    return selected.getPosition();
                }
            }
            return null;
        };
    }

    @Override
    protected Consumer<Point2D> retrieveSetter() {
        return point -> {
            LayerPainter cachedLayerPainter = parentPanel.getCachedLayerPainter();
            if (isLayerPainterEligible(cachedLayerPainter)) {
                BoundPointsPainter boundsPainter = ((ShipPainter) cachedLayerPainter).getBoundsPainter();
                BoundPoint selected = boundsPainter.getSelected();
                if (selected != null) {
                    boundsPainter.dragPointWithMirrorCheck(point);
                    EditDispatch.notifyTimedEditCommenced();
                }
            }
        };
    }

}
