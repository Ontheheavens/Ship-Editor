package oth.shipeditor.components.instrument.ship.weaponslots;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotsPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import java.awt.*;

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

        northContainer.add(new JPanel());

        slotPointContainer = new WeaponSlotList(model, northContainer);
        JScrollPane scrollableContainer = new JScrollPane(slotPointContainer);

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        Pair<JPanel, JCheckBox> reorderWidget = ComponentUtilities.createReorderCheckboxPanel(slotPointContainer);
        reorderCheckbox = reorderWidget.getSecond();
        northContainer.add(reorderWidget.getFirst());

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof SlotsPanelRepaintQueued) {
                this.repaint();
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
            }
        });
    }

}
