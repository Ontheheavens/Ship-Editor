package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.weapon.OffsetPoint;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 02.09.2023
 */
public class WeaponOffsetPainter extends AngledPointPainter {

    @Getter @Setter
    private List<OffsetPoint> offsetPoints;

    public WeaponOffsetPainter(WeaponPainter parent) {
        super(parent);
        this.setInteractionEnabled(false);
    }

    @Override
    protected List<OffsetPoint> getPointsIndex() {
        return offsetPoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof OffsetPoint checked) {
            offsetPoints.add(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof OffsetPoint checked) {
            offsetPoints.remove(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof OffsetPoint checked) {
            return offsetPoints.indexOf(checked);
        } else {
            throwIllegalPoint();
            return -1;
        }
    }

    @Override
    protected Class<OffsetPoint> getTypeReference() {
        return OffsetPoint.class;
    }

    @Override
    protected int getControlHotkey() {
        return 0;
    }

    @Override
    protected int getCreationHotkey() {
        return 0;
    }

    @Override
    protected void initHotkeys() {
        // TODO: weapon editing not implemented for now, consider later.
    }

    @Override
    protected void setControlHotkeyPressed(boolean pressed) {

    }

    @Override
    protected void setCreationHotkeyPressed(boolean pressed) {

    }

    @Override
    protected EditorInstrument getInstrumentType() {
        return null;
    }

    @Override
    protected void handleCreation(PointCreationQueued event) {

    }

    @Override
    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {

    }

}
