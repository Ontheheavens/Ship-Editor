package oth.shipeditor.components.instrument.ship.slots;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.communication.events.viewer.points.WeaponSlotInsertedConfirmed;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 19.11.2023
 */
public class WeaponSlotListPanel extends AbstractShipPropertiesPanel {

    @Getter
    private WeaponSlotList slotPointContainer;

    private SlotDataControlPane slotDataPane;

    private JCheckBox reorderCheckbox;

    private DefaultListModel<WeaponSlotPoint> model;

    WeaponSlotListPanel() {
        this.initPointListener();
    }

    private void refreshPointDataPane(WeaponSlotPoint slotPoint) {
        ShipPainter painter = (ShipPainter) getCachedLayerPainter();
        if (slotPoint != null) {
            painter = slotPoint.getParent();
            this.slotDataPane.refreshWithSelectedPoint(painter, slotPoint);
        } else {
            this.slotDataPane.refresh(painter);
        }
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        DefaultListModel<WeaponSlotPoint> newModel = new DefaultListModel<>();

        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) {
            this.model = newModel;
            this.slotPointContainer.setModel(newModel);

            fireClearingListeners(layerPainter);
            refreshPointDataPane(null);


            this.slotPointContainer.setEnabled(false);
            this.reorderCheckbox.setEnabled(false);
            return;
        }

        WeaponSlotPainter weaponSlotPainter = shipPainter.getWeaponSlotPainter();
        newModel.addAll(weaponSlotPainter.getPointsIndex());

        this.model = newModel;
        this.slotPointContainer.setModel(newModel);
        this.slotPointContainer.setEnabled(true);
        this.reorderCheckbox.setEnabled(true);

        fireRefresherListeners(layerPainter);
        refreshPointDataPane(weaponSlotPainter.getSelected());
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        this.model = new DefaultListModel<>();
        this.slotPointContainer = new WeaponSlotList(model, this::refreshPointDataPane);
        this.slotDataPane = new SlotDataControlPane(slotPointContainer);

        JPanel northContainer = new JPanel(new BorderLayout());
        var visibilityWidget = createSlotsVisibilityWidget();
        Map<JLabel, JComponent> visibilityWidgetMap = Map.of(visibilityWidget.getFirst(), visibilityWidget.getSecond());
        JPanel visibilityWidgetContainer = this.createWidgetsPanel(visibilityWidgetMap);
        visibilityWidgetContainer.setBorder(new EmptyBorder(4, 0, 3, 0));
        northContainer.add(visibilityWidgetContainer, BorderLayout.PAGE_START);

        ComponentUtilities.outfitPanelWithTitle(slotDataPane, "Slot Data");
        northContainer.add(slotDataPane, BorderLayout.CENTER);

        this.refreshPointDataPane(null);

        JScrollPane scrollableContainer = new JScrollPane(slotPointContainer);

        Pair<JPanel, JCheckBox> reorderWidget = ComponentUtilities.createReorderCheckboxPanel(slotPointContainer);
        reorderCheckbox = reorderWidget.getSecond();
        northContainer.add(reorderWidget.getFirst(), BorderLayout.PAGE_END);

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued checked) {
                if (checked.editorMode() != EditorInstrument.WEAPON_SLOTS) {
                    return;
                }
                WeaponSlotPainter cachedSlotPainter = getCachedSlotPainter();
                if (cachedSlotPainter != null) {
                    int[] cachedSelected = this.slotPointContainer.getSelectedIndices();
                    DefaultListModel<WeaponSlotPoint> newModel = new DefaultListModel<>();
                    newModel.addAll(cachedSlotPainter.getPointsIndex());

                    this.model = newModel;
                    this.slotPointContainer.setModel(newModel);
                    this.slotPointContainer.setSelectedIndices(cachedSelected);
                    if (!this.model.isEmpty() && cachedSelected.length > 0) {
                        this.slotPointContainer.ensureIndexIsVisible(cachedSelected[0]);
                    }
                }

                this.refreshPointDataPane(null);
            }
        });
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof WeaponSlotPoint point) {
                model.addElement(point);
                slotPointContainer.setSelectedIndex(model.indexOf(point));
                this.refreshPointDataPane(null);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof WeaponSlotInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                slotPointContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
                this.refreshPointDataPane(null);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof WeaponSlotPoint point) {
                model.removeElement(point);
                this.refreshPointDataPane(null);
            }
        });
    }

    private WeaponSlotPainter getCachedSlotPainter() {
        LayerPainter cachedLayerPainter = getCachedLayerPainter();
        if (cachedLayerPainter instanceof ShipPainter shipPainter && !shipPainter.isUninitialized()) {
            return shipPainter.getWeaponSlotPainter();
        }
        return null;
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createSlotsVisibilityWidget() {
        Function<LayerPainter, AbstractPointPainter> painterGetter = layerPainter -> {
            if (layerPainter instanceof ShipPainter shipPainter) {
                return shipPainter.getWeaponSlotPainter();
            }
            return null;
        };

        var opacityWidget = createVisibilityWidget(painterGetter);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText(StringValues.SLOTS_VIEW);

        return opacityWidget;
    }

}
