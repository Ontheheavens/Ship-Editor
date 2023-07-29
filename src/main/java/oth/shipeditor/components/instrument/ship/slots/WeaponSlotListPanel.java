package oth.shipeditor.components.instrument.ship.slots;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.entities.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class WeaponSlotListPanel extends JPanel {

    @Getter
    private final SlotList slotPointContainer;

    private DefaultListModel<WeaponSlotPoint> model = new DefaultListModel<>();

    WeaponSlotListPanel() {
        this.setLayout(new BorderLayout());
        slotPointContainer = new SlotList(model);
        JScrollPane scrollableContainer = new JScrollPane(slotPointContainer);

        this.add(scrollableContainer, BorderLayout.CENTER);
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                DefaultListModel<WeaponSlotPoint> newModel = new DefaultListModel<>();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    this.model = newModel;
                    this.slotPointContainer.setModel(newModel);
                    this.slotPointContainer.setEnabled(false);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    WeaponSlotPainter weaponSlotPainter = painter.getWeaponSlotPainter();
                    newModel.addAll(weaponSlotPainter.getPointsIndex());
                    this.slotPointContainer.setEnabled(true);
                } else {
                    this.slotPointContainer.setEnabled(false);
                }
                this.model = newModel;
                this.slotPointContainer.setModel(newModel);
            }
        });
    }

}
