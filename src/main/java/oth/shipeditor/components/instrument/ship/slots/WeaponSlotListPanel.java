package oth.shipeditor.components.instrument.ship.slots;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.components.SlotsPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.communication.events.viewer.points.WeaponSlotInsertedConfirmed;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class WeaponSlotListPanel extends JPanel {

    @Getter
    private final WeaponSlotList slotPointContainer;

    private final JCheckBox reorderCheckbox;

    private DefaultListModel<WeaponSlotPoint> model = new DefaultListModel<>();

    WeaponSlotListPanel() {
        this.setLayout(new BorderLayout());

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.PAGE_AXIS));

        JPanel visibilityWidgetContainer = this.createPainterVisibilityPanel();
        northContainer.add(visibilityWidgetContainer);

        ComponentUtilities.addSeparatorToBoxPanel(northContainer, 2);
        northContainer.add(Box.createRigidArea(new Dimension(10, 3)));

        JPanel slotInfoPanel = new JPanel();
        slotInfoPanel.setLayout(new BorderLayout());
        northContainer.add(slotInfoPanel);

        slotPointContainer = new WeaponSlotList(model, slotInfoPanel);
        slotPointContainer.refreshSlotControlPane();
        JScrollPane scrollableContainer = new JScrollPane(slotPointContainer);

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        Pair<JPanel, JCheckBox> reorderWidget = ComponentUtilities.createReorderCheckboxPanel(slotPointContainer);
        reorderCheckbox = reorderWidget.getSecond();
        northContainer.add(reorderWidget.getFirst());

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);

        this.initLayerListeners();
        this.initPointListener();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof SlotsPanelRepaintQueued) {
                this.repaint();
                slotPointContainer.refreshSlotControlPane();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SlotControlRepaintQueued) {
                slotPointContainer.refreshSlotControlPane();
                slotPointContainer.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                DefaultListModel<WeaponSlotPoint> newModel = new DefaultListModel<>();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    this.model = newModel;
                    this.slotPointContainer.setModel(newModel);
                    this.slotPointContainer.setEnabled(false);
                    reorderCheckbox.setEnabled(false);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    WeaponSlotPainter weaponSlotPainter = painter.getWeaponSlotPainter();
                    newModel.addAll(weaponSlotPainter.getPointsIndex());
                    this.slotPointContainer.setEnabled(true);
                    reorderCheckbox.setEnabled(true);
                } else {
                    this.slotPointContainer.setEnabled(false);
                    reorderCheckbox.setEnabled(false);
                }
                this.model = newModel;
                this.slotPointContainer.setModel(newModel);
                slotPointContainer.refreshSlotControlPane();
            }
        });
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof WeaponSlotPoint point) {
                model.addElement(point);
                slotPointContainer.setSelectedIndex(model.indexOf(point));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof WeaponSlotInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                slotPointContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof WeaponSlotPoint point) {
                model.removeElement(point);
            }
        });
    }

    @SuppressWarnings("MethodMayBeStatic")
    private JPanel createPainterVisibilityPanel() {
        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            WeaponSlotPainter slotPainter = checked.getWeaponSlotPainter();
            PainterVisibility valueOfLayer = slotPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };

        return ComponentUtilities.createVisibilityWidget(visibilityList,
                WeaponSlotPainter.class, selectionAction, "");
    }

}
