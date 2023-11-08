package oth.shipeditor.utility.components.widgets;

import lombok.Getter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.LayerPropertiesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 08.11.2023
 */
@Getter
public abstract class PointLocationWidget extends LayerPropertiesPanel {

    private TwinSpinnerPanel twinSpinnerPanel;

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        boolean uninitialized = layerPainter instanceof ShipPainter shipPainter && shipPainter.isUninitialized();
        if (layerPainter == null || uninitialized) {
            fireClearingListeners(null);
            return;
        }
        if (!isLayerPainterEligible(layerPainter)) {
            fireClearingListeners(layerPainter);
            return;
        }

        fireRefresherListeners(layerPainter);
    }

    protected abstract boolean isLayerPainterEligible(LayerPainter layerPainter);

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());
        Point2D initialPoint = new Point2D.Double();
        Consumer<Point2D> pointSetter = changed -> {
            if (isWidgetsReadyForInput()) {
                Consumer<Point2D> setter = retrieveSetter();
                setter.accept(changed);
                EditDispatch.notifyTimedEditCommenced();
                processChange();
            }
        };

        twinSpinnerPanel = Spinners.createLocationSpinners(initialPoint,
                retrieveGetter(), pointSetter);

        registerWidgetListeners(twinSpinnerPanel, layer -> {
            twinSpinnerPanel.clear();
            twinSpinnerPanel.disable();
        }, layer -> {
            Supplier<Point2D> getter = retrieveGetter();
            Point2D existing = getter.get();

            if (existing != null) {
                twinSpinnerPanel.enable();

                JSpinner firstSpinner = twinSpinnerPanel.getFirstSpinner();
                firstSpinner.setValue(existing.getX());

                JSpinner secondSpinner = twinSpinnerPanel.getSecondSpinner();
                secondSpinner.setValue(existing.getY());
            } else {
                twinSpinnerPanel.clear();
                twinSpinnerPanel.disable();
            }
        });

        twinSpinnerPanel.clear();
        twinSpinnerPanel.disable();

        ComponentUtilities.outfitPanelWithTitle(twinSpinnerPanel,
                new Insets(1, 0, 0, 0), getPanelTitleText());

        Dimension containerPreferredSize = twinSpinnerPanel.getPreferredSize();
        int width = twinSpinnerPanel.getMaximumSize().width;
        Dimension maximumSize = new Dimension(width, containerPreferredSize.height);
        twinSpinnerPanel.setMaximumSize(maximumSize);

        this.add(twinSpinnerPanel, BorderLayout.CENTER);
    }

    protected abstract String getPanelTitleText();

    /**
     * Should account for changing entities, e.g. different point painter instances.
     */
    protected abstract Supplier<Point2D> retrieveGetter();

    /**
     * Should account for changing entities.
     */
    protected abstract Consumer<Point2D> retrieveSetter();

}
