package oth.shipeditor.components.instrument.ship.bays;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.LaunchBayAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.LaunchBayRemoveConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.LaunchBayPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 19.11.2023
 */
public class LaunchBaysPanel extends AbstractShipPropertiesPanel {

    private LaunchBaysTree baysTree;

    private BayDataControlPane bayDataPanel;

    public LaunchBaysPanel() {
        this.initPointListener();
    }

    private void refreshPointDataPane(SlotPoint slotPoint) {
        ShipPainter painter = (ShipPainter) getCachedLayerPainter();
        if (slotPoint instanceof LaunchPortPoint portPoint) {
            painter = portPoint.getParent();
        }
        this.bayDataPanel.refresh(painter);
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued checked) {
                if (checked.editorMode() == EditorInstrument.LAUNCH_BAYS) {
                    this.baysTree.reloadModel();
                    this.refreshPointDataPane(null);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LaunchBayAddConfirmed checked) {
                var added = checked.added();
                var index = checked.index();

                if (index == -1) {
                    baysTree.addBay(added);
                } else {
                    baysTree.insertBay(added, index);
                }
                this.refreshPointDataPane(null);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LaunchBayRemoveConfirmed checked) {
                baysTree.removeBay(checked.removed());
                this.refreshPointDataPane(null);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof LaunchPortPoint point) {
                baysTree.addPort(point);
                this.refreshPointDataPane(point);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof LaunchPortPoint point) {
                baysTree.removePort(point);
                this.refreshPointDataPane(point);
            }
        });
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        baysTree.clearRoot();
        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) {
            fireClearingListeners(layerPainter);
            refreshPointDataPane(null);
            baysTree.setEnabled(false);
            return;
        }

        baysTree.setEnabled(true);
        LaunchBayPainter bayPainter = shipPainter.getBayPainter();
        baysTree.repopulateTree(bayPainter);
        baysTree.repaint();

        fireRefresherListeners(layerPainter);
        refreshPointDataPane(bayPainter.getSelected());
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Bays");
        baysTree = new LaunchBaysTree(root, this::refreshPointDataPane);

        JScrollPane scrollableContainer = new JScrollPane(baysTree);

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setBorder(new EmptyBorder(0, 0, 4, 0));
        var visibilityWidget = createBaysVisibilityWidget();
        Map<JLabel, JComponent> visibilityWidgetMap = Map.of(visibilityWidget.getFirst(), visibilityWidget.getSecond());
        JPanel visibilityWidgetContainer = this.createWidgetsPanel(visibilityWidgetMap);
        visibilityWidgetContainer.setBorder(new EmptyBorder(4, 0, 3, 0));
        northContainer.add(visibilityWidgetContainer, BorderLayout.PAGE_START);

        this.bayDataPanel = new BayDataControlPane();
        ComponentUtilities.outfitPanelWithTitle(bayDataPanel, "Bay Data");
        northContainer.add(bayDataPanel, BorderLayout.CENTER);

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createBaysVisibilityWidget() {
        BooleanSupplier readinessChecker = this::isWidgetsReadyForInput;
        Consumer<PainterVisibility> visibilitySetter = changedValue -> {
            LayerPainter cachedLayerPainter = getCachedLayerPainter();
            if (cachedLayerPainter != null) {
                LaunchBayPainter bayPainter = ((ShipPainter) cachedLayerPainter).getBayPainter();
                bayPainter.setVisibilityMode(changedValue);
                processChange();
            }
        };

        BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener = this::registerWidgetClearer;
        BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener = this::registerWidgetRefresher;

        Function<LayerPainter, PainterVisibility> visibilityGetter = layerPainter -> {
            LaunchBayPainter bayPainter = ((ShipPainter) layerPainter).getBayPainter();
            return bayPainter.getVisibilityMode();
        };

        var opacityWidget = PainterVisibility.createVisibilityWidget(
                readinessChecker, visibilityGetter, visibilitySetter,
                clearerListener, refresherListener
        );

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText("Bays view");

        return opacityWidget;
    }

}
