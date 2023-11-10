package oth.shipeditor.components.instrument;

import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.LayerPropertiesPanel;
import oth.shipeditor.utility.components.widgets.IncrementType;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.*;

/**
 * @author Ontheheavens
 * @since 07.11.2023
 */
public class LayerCircumstancePanel extends LayerPropertiesPanel {

    private PointLocationWidget locationWidget;

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        if (layerPainter == null) {
            fireClearingListeners(null);
            locationWidget.refresh(null);
            return;
        }

        fireRefresherListeners(layerPainter);
        locationWidget.refresh(layerPainter);
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());
        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var layerOpacityWidget = createLayerOpacitySlider();
        widgets.put(layerOpacityWidget.getFirst(), layerOpacityWidget.getSecond());

        var layerRotationWidget = createLayerRotationSpinner();
        widgets.put(layerRotationWidget.getFirst(), layerRotationWidget.getSecond());

        JPanel widgetsPanel = createWidgetsPanel(widgets);
        this.add(widgetsPanel, BorderLayout.PAGE_START);

        locationWidget = LayerCircumstancePanel.createAnchorLocationWidget();
        this.add(locationWidget, BorderLayout.CENTER);
    }

    private Pair<JLabel, JSlider> createLayerOpacitySlider() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<Float> opacitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                cachedLayerPainter.setSpriteOpacity(changedValue);
            }
            processChange();
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Function<LayerPainter, Float> opacityGetter = LayerPainter::getSpriteOpacity;

        Pair<JLabel, JSlider> opacityWidget = ComponentUtilities.createOpacityWidget(readinessChecker,
                opacityGetter, opacitySetter, clearerListener, refresherListener);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Sprite opacity:");

        return opacityWidget;
    }

    private Pair<JLabel, JSpinner> createLayerRotationSpinner() {
        double minimum = 0.0d;
        double maximum = 360.0d;
        double initial = 0.0d;
        SpinnerNumberModel rotationModel = new SpinnerNumberModel(initial, minimum, maximum, 1.0d);

        JSpinner rotationSpinner = Spinners.createWheelable(rotationModel, IncrementType.CHUNK);
        rotationSpinner.setEnabled(false);
        JLabel rotationLabel = new JLabel("Layer rotation");

        rotationSpinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = rotationModel.getNumber();
                double newRotation = modelNumber.doubleValue();
                double reversed = (360 - newRotation) % 360;

                LayerPainter layerPainter = getCachedLayerPainter();
                layerPainter.rotateLayer(reversed);
                processChange();
            }
        });

        registerWidgetListeners(rotationSpinner, layer -> {
            rotationModel.setValue(0);
            rotationSpinner.setEnabled(false);
        }, layerPainter -> {
            if (ControlPredicates.isRotationRoundingEnabled()) {
                rotationModel.setStepSize(1.0d);
            } else {
                rotationModel.setStepSize(0.005d);
            }
            double currentRotation = layerPainter.getRotationRadians();

            double currentClamped = Utility.clampAngleWithRounding(currentRotation);
            rotationModel.setValue(currentClamped);
            rotationSpinner.setEnabled(true);
        });

        return new Pair<>(rotationLabel, rotationSpinner);
    }

    private static PointLocationWidget createAnchorLocationWidget() {
        return new LayerAnchorLocationWidget();
    }

    private static class LayerAnchorLocationWidget extends PointLocationWidget {

        @Override
        protected boolean isLayerPainterEligible(LayerPainter layerPainter) {
            return layerPainter != null;
        }

        @Override
        protected String getPanelTitleText() {
            return "Anchor position";
        }

        @Override
        protected Supplier<Point2D> retrieveGetter() {
            return () -> {
                LayerPainter cachedLayerPainter = getCachedLayerPainter();
                if (cachedLayerPainter != null) {
                    return cachedLayerPainter.getAnchor();
                }
                return null;
            };
        }

        @Override
        protected Consumer<Point2D> retrieveSetter() {
            return point -> {
                LayerPainter cachedLayerPainter = getCachedLayerPainter();
                if (cachedLayerPainter != null) {
                    cachedLayerPainter.updateAnchorOffset(point);
                }
            };
        }

    }

}
