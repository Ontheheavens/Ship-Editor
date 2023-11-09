package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.CenterPointPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.LayerPropertiesPanel;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 09.11.2023
 */
public class CollisionPanel2 extends LayerPropertiesPanel {

    @Override
    public void refreshContent(LayerPainter layerPainter) {

    }

    @Override
    protected void populateContent() {

    }


    private Pair<JLabel, JSlider> createCollisionOpacityWidget() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<Float> opacitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                CenterPointPainter centerPointPainter = ((ShipPainter) cachedLayerPainter).getCenterPointPainter();
                centerPointPainter.setPaintOpacity(changedValue);
            }
            processChange();
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Pair<JLabel, JSlider> opacityWidget = ComponentUtilities.createOpacityWidget(readinessChecker,
                opacitySetter, clearerListener, refresherListener);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Painter opacity:");

        return opacityWidget;
    }

}
