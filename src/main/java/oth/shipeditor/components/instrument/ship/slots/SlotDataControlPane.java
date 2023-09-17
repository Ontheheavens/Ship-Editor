package oth.shipeditor.components.instrument.ship.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.instrument.ship.AbstractSlotValuesPanel;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Ontheheavens
 * @since 11.08.2023
 */
public class SlotDataControlPane extends AbstractSlotValuesPanel {

    private final WeaponSlotList slotList;

    @SuppressWarnings("TypeMayBeWeakened")
    SlotDataControlPane(WeaponSlotPoint slotPoint, WeaponSlotList parent) {
        super(slotPoint, true);
        this.slotList = parent;
    }

    @Override
    protected String getEntityName() {
        return "Slot";
    }

    @Override
    protected ActionListener getTypeSelectorListener(JComboBox<WeaponType> typeSelector) {
        return e -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) -> {
            WeaponType selectedType = (WeaponType) typeSelector.getSelectedItem();
            weaponSlotPainter.changeSlotsTypeWithMirrorCheck(selectedType, weaponSlotPoints);
        });
    }

    @Override
    protected ActionListener getMountSelectorListener(JComboBox<WeaponMount> mountSelector) {
        return e -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) -> {
            WeaponMount selectedMount = (WeaponMount) mountSelector.getSelectedItem();
            weaponSlotPainter.changeSlotsMountWithMirrorCheck(selectedMount, weaponSlotPoints);
        });
    }

    @Override
    protected ActionListener getSizeSelectorListener(JComboBox<WeaponSize> sizeSelector) {
        return e -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) -> {
            WeaponSize selectedSize = (WeaponSize) sizeSelector.getSelectedItem();
            weaponSlotPainter.changeSlotsSizeWithMirrorCheck(selectedSize, weaponSlotPoints);
        });
    }

    @Override
    protected ChangeListener getAngleChangeListener(JSpinner spinner,
                                                    SpinnerNumberModel spinnerNumberModel,
                                                    SlotData slotPoint) {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                WeaponSlotPoint weaponSlotPoint = (WeaponSlotPoint) slotPoint;
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();

                ShipPainter slotParent = weaponSlotPoint.getParent();
                WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
                weaponSlotPainter.changePointAngleWithMirrorCheck(weaponSlotPoint, current);

                spinner.removeChangeListener(this);
            }
        };
    }

    @Override
    protected ChangeListener getArcChangeListener(JSpinner spinner,
                                                  SpinnerNumberModel spinnerNumberModel,
                                                  SlotData slotPoint) {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                WeaponSlotPoint weaponSlotPoint = (WeaponSlotPoint) slotPoint;

                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();
                ShipPainter slotParent = weaponSlotPoint.getParent();
                WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
                weaponSlotPainter.changeArcWithMirrorCheck(weaponSlotPoint, current);

                spinner.removeChangeListener(this);
            }
        };
    }

    private void actOnSelectedValues(BiConsumer<WeaponSlotPainter, List<WeaponSlotPoint>> action) {
        WeaponSlotPoint selectedValue = slotList.getSelectedValue();
        if (selectedValue == null) return;
        ShipPainter parentLayer = selectedValue.getParent();
        WeaponSlotPainter slotPainter = parentLayer.getWeaponSlotPainter();
        List<WeaponSlotPoint> selectedValuesList = slotList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty()) {
            action.accept(slotPainter, selectedValuesList);
            EventBus.publish(new ViewerRepaintQueued());
        }
    }

}
