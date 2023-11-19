package oth.shipeditor.components.instrument.ship.slots;

import oth.shipeditor.components.instrument.ship.shared.AbstractSlotValuesPanel;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 19.11.2023
 */
public class SlotDataControlPane extends AbstractSlotValuesPanel {

    private final WeaponSlotList slotList;

    SlotDataControlPane(WeaponSlotList weaponSlotList) {
        super(true);
        this.slotList = weaponSlotList;
    }

    @Override
    protected String getEntityName() {
        return "Slot";
    }

    @Override
    protected WeaponSlotPoint getSelectedFromLayer(LayerPainter layerPainter) {
        return Utility.getSelectedFromLayer(layerPainter);
    }

    @Override
    protected String getNextUniqueID() {
        var layer = StaticController.getActiveLayer();
        if (!(layer instanceof ShipLayer shipLayer)) return null;
        var shipPainter = shipLayer.getPainter();
        if (shipPainter == null || shipPainter.isUninitialized()) return null;

        var slotPainter = shipPainter.getWeaponSlotPainter();
        return slotPainter.generateUniqueSlotID();
    }

    @Override
    protected Consumer<String> getIDSetter() {
        return slotID -> actOnSelectedValues((weaponSlotPainter, slotPoints) ->
                        weaponSlotPainter.changeSlotsIDWithMirrorCheck(slotID, slotPoints));
    }

    @Override
    protected Consumer<WeaponType> getTypeSetter() {
        return type -> actOnSelectedValues((weaponSlotPainter, slotPoints) ->
                weaponSlotPainter.changeSlotsTypeWithMirrorCheck(type, slotPoints));
    }

    @Override
    protected Consumer<WeaponMount> getMountSetter() {
        return mount -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) ->
                weaponSlotPainter.changeSlotsMountWithMirrorCheck(mount, weaponSlotPoints));
    }

    @Override
    protected Consumer<WeaponSize> getSizeSetter() {
        return size -> this.actOnSelectedValues((weaponSlotPainter, weaponSlotPoints) ->
                weaponSlotPainter.changeSlotsSizeWithMirrorCheck(size, weaponSlotPoints));
    }

    @Override
    protected Consumer<Double> getAngleSetter() {
        return angle -> {
            ShipPainter slotParent = getCachedLayerPainter();
            WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
            WeaponSlotPoint selectedFromLayer = getSelectedFromLayer(slotParent);
            weaponSlotPainter.changePointAngleWithMirrorCheck(selectedFromLayer, angle);
        };
    }

    @Override
    protected Consumer<Double> getArcSetter() {
        return arc -> {
            ShipPainter slotParent = getCachedLayerPainter();
            WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
            WeaponSlotPoint selectedFromLayer = getSelectedFromLayer(slotParent);
            weaponSlotPainter.changeArcWithMirrorCheck(selectedFromLayer, arc);
        };
    }

    @Override
    protected Consumer<Double> getRenderOrderSetter() {
        return renderOrder -> {
            ShipPainter slotParent = getCachedLayerPainter();
            WeaponSlotPainter weaponSlotPainter = slotParent.getWeaponSlotPainter();
            WeaponSlotPoint selectedFromLayer = getSelectedFromLayer(slotParent);
            int renderOrderValue = renderOrder.intValue();
            weaponSlotPainter.changeRenderOrderWithMirrorCheck(selectedFromLayer, renderOrderValue);

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
        }
    }

}
