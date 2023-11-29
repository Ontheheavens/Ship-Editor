package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.ShieldPointPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.IncrementType;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 10.11.2023
 */
public class ShieldPanel extends AbstractCenterPanel {

    private PointLocationWidget shieldCenterWidget;

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) {
            fireClearingListeners(layerPainter);
            shieldCenterWidget.refresh(null);
            return;
        }

        fireRefresherListeners(layerPainter);
        shieldCenterWidget.refresh(layerPainter);
    }

    @Override
    protected EditorInstrument getMode() {
        return EditorInstrument.SHIELD;
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        JPanel topContainer = new JPanel(new BorderLayout());

        Map<JLabel, JComponent> topWidgets = new LinkedHashMap<>();

        var collisionOpacityWidget = createShieldOpacityWidget();
        topWidgets.put(collisionOpacityWidget.getFirst(), collisionOpacityWidget.getSecond());

        var collisionVisibilityWidget = createShieldVisibilityWidget();
        topWidgets.put(collisionVisibilityWidget.getFirst(), collisionVisibilityWidget.getSecond());

        Border bottomPadding = new EmptyBorder(0, 0, 4, 0);

        JPanel topWidgetsPanel = createWidgetsPanel(topWidgets);
        topWidgetsPanel.setBorder(bottomPadding);
        topContainer.add(topWidgetsPanel, BorderLayout.PAGE_START);
        shieldCenterWidget = createShieldCenterLocationWidget();
        topContainer.add(shieldCenterWidget, BorderLayout.CENTER);
        this.add(topContainer, BorderLayout.PAGE_START);

        JPanel centerContainer = new JPanel(new BorderLayout());

        var collisionRadiusWidget = createShieldRadiusSpinner();
        Map<JLabel, JComponent> centerWidgets = Map.of(
                collisionRadiusWidget.getFirst(), collisionRadiusWidget.getSecond()
        );

        JPanel centerWidgetsPanel = createWidgetsPanel(centerWidgets);
        centerWidgetsPanel.setBorder(bottomPadding);
        centerContainer.add(centerWidgetsPanel, BorderLayout.PAGE_START);

        this.add(centerContainer, BorderLayout.CENTER);
    }

    private Pair<JLabel, JSlider> createShieldOpacityWidget() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<Float> opacitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                ShieldPointPainter shieldPointPainter = ((ShipPainter) cachedLayerPainter).getShieldPointPainter();
                shieldPointPainter.setPaintOpacity(changedValue);
                processChange();
            }
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Function<LayerPainter, Float> opacityGetter = layerPainter -> {
            ShieldPointPainter shieldPointPainter = ((ShipPainter) layerPainter).getShieldPointPainter();
            return shieldPointPainter.getPaintOpacity();
        };

        Pair<JLabel, JSlider> opacityWidget = ComponentUtilities.createOpacityWidget(readinessChecker,
                opacityGetter, opacitySetter, clearerListener, refresherListener);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Shield opacity:");

        return opacityWidget;
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createShieldVisibilityWidget() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<PainterVisibility> visibilitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                ShieldPointPainter shieldPointPainter = ((ShipPainter) cachedLayerPainter).getShieldPointPainter();
                shieldPointPainter.setVisibilityMode(changedValue);
                processChange();
            }
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Function<LayerPainter, PainterVisibility> visibilityGetter = layerPainter -> {
            ShieldPointPainter shieldPointPainter = ((ShipPainter) layerPainter).getShieldPointPainter();
            return shieldPointPainter.getVisibilityMode();
        };

        var opacityWidget = PainterVisibility.createVisibilityWidget(
                readinessChecker, visibilityGetter, visibilitySetter, clearerListener, refresherListener
        );

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Shield view");

        return opacityWidget;
    }

    private Pair<JLabel, JSpinner> createShieldRadiusSpinner() {
        double minimum = 0.0d;
        double maximum = Double.MAX_VALUE;
        double initial = 0.0d;
        SpinnerNumberModel numberModel = new SpinnerNumberModel(initial, minimum, maximum, 1.0d);

        JSpinner radiusSpinner = Spinners.createWheelable(numberModel, IncrementType.CHUNK);
        radiusSpinner.setEnabled(false);
        JLabel radiusLabel = new JLabel(StringValues.SHIELD_RADIUS);

        radiusSpinner.addChangeListener(e -> {
            if (!isWidgetsReadyForInput()) return;
            Number modelNumber = numberModel.getNumber();
            double newRadius = modelNumber.doubleValue();

            LayerPainter layerPainter = getCachedLayerPainter();
            ShipPainter shipPainter = (ShipPainter) layerPainter;
            ShieldPointPainter shieldPointPainter = shipPainter.getShieldPointPainter();
            ShieldCenterPoint shieldCenterPoint = shieldPointPainter.getShieldCenterPoint();
            EditDispatch.postShieldRadiusChanged(shieldCenterPoint, (float) newRadius);
            processChange();
        });

        registerWidgetListeners(radiusSpinner, layer -> {
            numberModel.setValue(0.0d);
            radiusSpinner.setEnabled(false);
        }, layerPainter -> {
            ShipPainter shipPainter = (ShipPainter) layerPainter;
            ShieldPointPainter shieldPointPainter = shipPainter.getShieldPointPainter();
            ShieldCenterPoint shieldCenterPoint = shieldPointPainter.getShieldCenterPoint();
            double currentRadius = shieldCenterPoint.getShieldRadius();

            numberModel.setValue(currentRadius);
            radiusSpinner.setEnabled(true);
        });

        return new Pair<>(radiusLabel, radiusSpinner);
    }

    private PointLocationWidget createShieldCenterLocationWidget() {
        return new ShieldCenterLocationWidget(this);
    }

}
