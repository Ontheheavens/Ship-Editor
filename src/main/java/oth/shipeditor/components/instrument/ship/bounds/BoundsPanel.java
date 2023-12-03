package oth.shipeditor.components.instrument.ship.bounds;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.BoundPointsPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;
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
 * @since 11.11.2023
 */
public class BoundsPanel extends AbstractShipPropertiesPanel {

    @Getter
    private BoundList boundList;

    private DefaultListModel<BoundPoint> model;

    private PointLocationWidget selectedBoundWidget;

    public BoundsPanel() {
        this.initPointListeners();
    }

    private void initPointListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode == EditorInstrument.BOUNDS) {
                    LayerPainter cachedLayerPainter = getCachedLayerPainter();
                    if (cachedLayerPainter != null) {
                        DefaultListModel<BoundPoint> newModel = new DefaultListModel<>();
                        BoundPointsPainter boundsPainter = ((ShipPainter) cachedLayerPainter).getBoundsPainter();
                        newModel.addAll(boundsPainter.getPointsIndex());

                        this.model = newModel;
                        this.boundList.setModel(newModel);
                    }
                    this.refresh(cachedLayerPainter);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.addElement(point);
                boundList.setSelectedIndex(model.indexOf(point));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof BoundInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                boundList.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.removeElement(point);
            }
        });
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        DefaultListModel<BoundPoint> newModel = new DefaultListModel<>();

        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) {
            this.model = newModel;
            this.boundList.setModel(newModel);

            fireClearingListeners(layerPainter);
            selectedBoundWidget.refresh(null);

            this.boundList.setEnabled(false);
            return;
        }

        BoundPointsPainter newBoundsPainter = shipPainter.getBoundsPainter();
        newModel.addAll(newBoundsPainter.getPointsIndex());

        this.model = newModel;
        this.boundList.setModel(newModel);
        this.boundList.setEnabled(true);

        fireRefresherListeners(layerPainter);
        selectedBoundWidget.refresh(layerPainter);
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        JPanel topContainer = new JPanel(new BorderLayout());

        Map<JLabel, JComponent> topWidgets = new LinkedHashMap<>();

        var boundsOpacityWidget = createBoundsOpacityWidget();
        topWidgets.put(boundsOpacityWidget.getFirst(), boundsOpacityWidget.getSecond());

        var boundsVisibilityWidget = createBoundsVisibilityWidget();
        topWidgets.put(boundsVisibilityWidget.getFirst(), boundsVisibilityWidget.getSecond());

        Border bottomPadding = new EmptyBorder(0, 0, 4, 0);

        JPanel topWidgetsPanel = createWidgetsPanel(topWidgets);
        topWidgetsPanel.setBorder(bottomPadding);
        topContainer.add(topWidgetsPanel, BorderLayout.PAGE_START);
        selectedBoundWidget = createSelectedBoundLocationWidget();
        topContainer.add(selectedBoundWidget, BorderLayout.CENTER);
        this.add(topContainer, BorderLayout.PAGE_START);

        JPanel centerContainer = new JPanel(new BorderLayout());

        model = new DefaultListModel<>();
        boundList = new BoundList(model, () -> selectedBoundWidget.refresh(getCachedLayerPainter()));
        JScrollPane scrollableContainer = new JScrollPane(boundList);

        var reorderWidget = ComponentUtilities.createReorderCheckboxPanel(boundList);
        var reorderCheckbox = reorderWidget.getSecond();
        registerWidgetListeners(reorderCheckbox, layerPainter -> reorderCheckbox.setEnabled(false),
                layerPainter -> reorderCheckbox.setEnabled(true));
        centerContainer.add(reorderWidget.getFirst(), BorderLayout.PAGE_START);

        centerContainer.add(scrollableContainer, BorderLayout.CENTER);

        this.add(centerContainer, BorderLayout.CENTER);
    }

    private Pair<JLabel, JSlider> createBoundsOpacityWidget() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<Float> opacitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                BoundPointsPainter boundsPainter = ((ShipPainter) cachedLayerPainter).getBoundsPainter();
                boundsPainter.setPaintOpacity(changedValue);
                processChange();
            }
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Function<LayerPainter, Float> opacityGetter = layerPainter -> {
            BoundPointsPainter boundsPainter = ((ShipPainter) layerPainter).getBoundsPainter();
            return boundsPainter.getPaintOpacity();
        };

        Pair<JLabel, JSlider> opacityWidget = ComponentUtilities.createOpacityWidget(readinessChecker,
                opacityGetter, opacitySetter, clearerListener, refresherListener);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Bounds opacity:");

        return opacityWidget;
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createBoundsVisibilityWidget() {
        Function<LayerPainter, AbstractPointPainter> painterGetter = layerPainter -> {
            if (layerPainter instanceof ShipPainter shipPainter) {
                return shipPainter.getBoundsPainter();
            }
            return null;
        };

        var opacityWidget = createVisibilityWidget(painterGetter);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText(StringValues.BOUNDS_VIEW);

        return opacityWidget;
    }

    private PointLocationWidget createSelectedBoundLocationWidget() {
        return new BoundLocationWidget(this);
    }

}
