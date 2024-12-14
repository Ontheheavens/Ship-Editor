package oth.shipeditor.utility.components.widgets;

import lombok.Getter;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
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

    protected TwinSpinnerPanel createSpinnerPanel(Point2D initialPoint, Consumer<Point2D> pointSetter) {
        TwinSpinnerPanel spinnerPanel = Spinners.createLocationSpinners(initialPoint,
                retrieveGetter(), pointSetter);
        spinnerPanel.setToolTipText(StringValues.POINT_LOCATION_IN_WORLD_COORDINATES);
        return spinnerPanel;
    }

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

        twinSpinnerPanel = createSpinnerPanel(initialPoint, pointSetter);

        registerWidgetListeners(twinSpinnerPanel, layer -> {
            twinSpinnerPanel.clear();
            twinSpinnerPanel.setEnabled(false);
        }, layer -> {
            Supplier<Point2D> getter = retrieveGetter();
            Point2D existing = getter.get();

            if (existing != null) {
                twinSpinnerPanel.setEnabled(true);

                JSpinner firstSpinner = twinSpinnerPanel.getFirstSpinner();
                firstSpinner.setValue(existing.getX());

                JSpinner secondSpinner = twinSpinnerPanel.getSecondSpinner();
                secondSpinner.setValue(existing.getY());
            } else {
                twinSpinnerPanel.clear();
                twinSpinnerPanel.setEnabled(false);
            }
        });

        twinSpinnerPanel.clear();
        twinSpinnerPanel.setEnabled(false);

        addPanelTitle();

        Dimension containerPreferredSize = twinSpinnerPanel.getPreferredSize();
        int width = twinSpinnerPanel.getMaximumSize().width;
        Dimension maximumSize = new Dimension(width, containerPreferredSize.height);
        twinSpinnerPanel.setMaximumSize(maximumSize);

        this.add(twinSpinnerPanel, BorderLayout.CENTER);
    }

    private void addPanelTitle() {
        Insets insets = new Insets(1, 0, 0, 0);
        ComponentUtilities.outfitPanelWithTitle(this, insets, getPanelTitleText());
    }

    protected JPanel createDependentCoordinatesLabel(String name) {
        JLabel coordsNameLabel = new JLabel(name);

        String coordinatesHint = "Point location depends on coordinate system";
        CoordsDisplayMode coordsMode = StaticController.getCoordsMode();
        String currentMode = "Current system: " + coordsMode.getShortName();
        coordsNameLabel.setToolTipText(Utility.getWithLinebreaks(coordinatesHint, currentMode));

        JLabel coordsDisplayLabel = new JLabel(StringValues.NOT_INITIALIZED);

        registerWidgetListeners(coordsDisplayLabel,
                layer -> coordsDisplayLabel.setText(StringValues.NOT_INITIALIZED),
                layer -> {
                    Supplier<Point2D> getter = retrieveGetter();
                    Point2D existing = getter.get();
                    if (existing != null) {
                        Point2D translated = Utility.getPointCoordinatesForDisplay(existing);
                        coordsDisplayLabel.setText(Utility.getPointPositionText(translated));
                    } else {
                        coordsDisplayLabel.setText(StringValues.NOT_INITIALIZED);
                    }
                });

        var container = createWidgetsPanel(Map.of(coordsNameLabel, coordsDisplayLabel));
        container.setBorder(new EmptyBorder(0, 0, 5, 0));
        return container;
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
