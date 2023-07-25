package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
public class WeaponSlotPainter extends MirrorablePointPainter{

    @Getter
    private final List<WeaponSlotPoint> slotPoints;

    public WeaponSlotPainter(LayerPainter parent) {
        super(parent);
        this.slotPoints = new ArrayList<>();

//        this.initHotkeys();
//        this.initModeListener();
//        this.initCreationListener();

        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.WEAPON_SLOTS);
    }

    @Override
    public List<WeaponSlotPoint> getPointsIndex() {
        return slotPoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            slotPoints.add(checked);
        } else {
            throw new IllegalArgumentException("Attempted to add incompatible point to WeaponSlotPainter!");
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            slotPoints.remove(checked);
        } else {
            throw new IllegalArgumentException("Attempted to remove incompatible point from WeaponSlotPainter!");
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            return slotPoints.indexOf(checked);
        } else {
            throw new IllegalArgumentException("Attempted to access incompatible point in WeaponSlotPainter!");
        }
    }

    @Override
    protected Class<WeaponSlotPoint> getTypeReference() {
        return WeaponSlotPoint.class;
    }

}
