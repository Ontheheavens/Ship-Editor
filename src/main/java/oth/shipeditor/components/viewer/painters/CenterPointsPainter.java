package oth.shipeditor.components.viewer.painters;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Also intended to handle collision and shield radii and their painting.
 * @author Ontheheavens
 * @since 09.06.2023
 */
public class CenterPointsPainter extends AbstractPointPainter {

    private final List<BaseWorldPoint> points = new ArrayList<>();

    public CenterPointsPainter() {
        this.initModeListening();
    }

    private void initModeListening() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                this.setInteractionEnabled(checked.newMode() == InstrumentMode.CENTERS);
            }
        });
    }

    @Override
    public List<BaseWorldPoint> getPointsIndex() {
        return points;
    }

    @Override
    public void removePoint(BaseWorldPoint point) {
        if (point instanceof ShipCenterPoint) return;
        super.removePoint(point);
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        points.add(point);
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        points.remove(point);
    }

    @Override
    protected BaseWorldPoint getTypeReference() {
        return new BaseWorldPoint();
    }
}