package oth.shipeditor.components.instrument.ship.engines;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.EngineInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.EngineSlotPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 22.11.2023
 */
public class EnginesPanel extends AbstractShipPropertiesPanel {

    @Getter
    private EngineList enginesContainer;

    private EngineDataPanel dataPanel;

    private JCheckBox reorderCheckbox;

    private DefaultListModel<EnginePoint> model;

    public EnginesPanel() {
        this.initPointListener();
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        DefaultListModel<EnginePoint> newModel = new DefaultListModel<>();

        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) {
            this.model = newModel;
            this.enginesContainer.setModel(newModel);

            fireClearingListeners(layerPainter);
            refreshEngineControlPane(null);


            this.enginesContainer.setEnabled(false);
            this.reorderCheckbox.setEnabled(false);
            return;
        }

        EngineSlotPainter enginePainter = shipPainter.getEnginePainter();
        newModel.addAll(enginePainter.getPointsIndex());

        this.model = newModel;
        this.enginesContainer.setModel(newModel);
        this.enginesContainer.setEnabled(true);
        this.reorderCheckbox.setEnabled(true);

        fireRefresherListeners(layerPainter);
        refreshEngineControlPane(enginePainter.getSelected());
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        this.model = new DefaultListModel<>();
        this.enginesContainer = new EngineList(model, this::refreshEngineControlPane);
        this.dataPanel = new EngineDataPanel();

        JPanel northContainer = new JPanel(new BorderLayout());
        var visibilityWidget = createEnginesVisibilityWidget();
        Map<JLabel, JComponent> visibilityWidgetMap = Map.of(visibilityWidget.getFirst(), visibilityWidget.getSecond());
        JPanel visibilityWidgetContainer = this.createWidgetsPanel(visibilityWidgetMap);
        visibilityWidgetContainer.setBorder(new EmptyBorder(4, 0, 3, 0));
        northContainer.add(visibilityWidgetContainer, BorderLayout.PAGE_START);

        ComponentUtilities.outfitPanelWithTitle(dataPanel, "Engine Data");
        northContainer.add(dataPanel, BorderLayout.CENTER);

        this.refreshEngineControlPane(null);

        JScrollPane scrollableContainer = new JScrollPane(enginesContainer);

        Pair<JPanel, JCheckBox> reorderWidget = ComponentUtilities.createReorderCheckboxPanel(enginesContainer);
        reorderCheckbox = reorderWidget.getSecond();
        northContainer.add(reorderWidget.getFirst(), BorderLayout.PAGE_END);

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);
    }

    private void refreshEngineControlPane(EnginePoint engine) {
        ShipPainter painter = (ShipPainter) getCachedLayerPainter();
        if (engine != null) {
            painter = (ShipPainter) engine.getParent();
        }
        this.dataPanel.refresh(painter);
    }

    private EngineSlotPainter getCachedEnginePainter() {
        LayerPainter cachedLayerPainter = getCachedLayerPainter();
        if (cachedLayerPainter instanceof ShipPainter shipPainter && !shipPainter.isUninitialized()) {
            return shipPainter.getEnginePainter();
        }
        return null;
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode != EditorInstrument.ENGINES) {
                    return;
                }
                EngineSlotPainter cachedEnginePainter = getCachedEnginePainter();
                if (cachedEnginePainter != null) {
                    int[] cachedSelected = this.enginesContainer.getSelectedIndices();
                    DefaultListModel<EnginePoint> newModel = new DefaultListModel<>();
                    newModel.addAll(cachedEnginePainter.getPointsIndex());

                    this.model = newModel;
                    this.enginesContainer.setModel(newModel);
                    this.enginesContainer.setSelectedIndices(cachedSelected);
                    if (!this.model.isEmpty() && cachedSelected.length > 0) {
                        this.enginesContainer.ensureIndexIsVisible(cachedSelected[0]);
                    }
                }

                this.refreshEngineControlPane(null);
            }
        });
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof EnginePoint point) {
                model.removeElement(point);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof EngineInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                enginesContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof EnginePoint point) {
                model.addElement(point);
                enginesContainer.setSelectedIndex(model.indexOf(point));
            }
        });
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createEnginesVisibilityWidget() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<PainterVisibility> visibilitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                EngineSlotPainter enginePainter = ((ShipPainter) cachedLayerPainter).getEnginePainter();
                enginePainter.setVisibilityMode(changedValue);
                processChange();
            }
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Function<LayerPainter, PainterVisibility> visibilityGetter = layerPainter -> {
            EngineSlotPainter enginePainter = ((ShipPainter) layerPainter).getEnginePainter();
            return enginePainter.getVisibilityMode();
        };

        var opacityWidget = PainterVisibility.createVisibilityWidget(
                readinessChecker, visibilityGetter, visibilitySetter,
                clearerListener, refresherListener
        );

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Engines view");

        return opacityWidget;
    }

}
