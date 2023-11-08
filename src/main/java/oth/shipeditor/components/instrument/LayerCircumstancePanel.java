package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerOpacityChangeQueued;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.LayerPropertiesPanel;
import oth.shipeditor.utility.components.widgets.IncrementType;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        Pair<JLabel, JSlider> baseWidgets = ComponentUtilities.createOpacityWidget();

        JLabel opacityLabel = baseWidgets.getFirst();
        opacityLabel.setText("Sprite opacity:");
        JSlider opacitySlider = baseWidgets.getSecond();

        opacitySlider.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                int opacity = opacitySlider.getValue();
                opacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
                float changedValue = opacity / 100.0f;
                EventBus.publish(new LayerOpacityChangeQueued(changedValue));
                processChange();
            }
        });

        registerWidgetListeners(opacitySlider, layer -> {
            opacitySlider.setValue(100);
            opacitySlider.setEnabled(false);
            opacityLabel.setToolTipText(StringValues.NOT_INITIALIZED);
        }, layerPainter -> {
            // Refresh code is expected to make sure this block never gets called if layer does not have a painter.
            int value = (int) (layerPainter.getSpriteOpacity() * 100.0f);
            opacityLabel.setToolTipText(StringValues.CURRENT_VALUE + value + "%");
            opacitySlider.setValue(value);
            opacitySlider.setEnabled(true);
        });

        return baseWidgets;
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
            return "Layer Anchor";
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
                    cachedLayerPainter.setAnchor(point);
                }
            };
        }

    }

}
