package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public class EngineSlotPainter extends AngledPointPainter {

    @Getter @Setter
    private List<EnginePoint> enginePoints;

    public EngineSlotPainter(ShipPainter parent) {
        super(parent);
        this.enginePoints = new ArrayList<>();
    }

    @Override
    protected ShipInstrument getInstrumentType() {
        return ShipInstrument.ENGINES;
    }

    @Override
    protected void handleCreation(PointCreationQueued event) {
        // TODO!
    }

    @Override
    public List<EnginePoint> getPointsIndex() {
        return enginePoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof EnginePoint checked) {
            enginePoints.add(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof EnginePoint checked) {
            enginePoints.remove(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof EnginePoint checked) {
            return enginePoints.indexOf(checked);
        } else {
            throwIllegalPoint();
            return -1;
        }
    }

    @Override
    protected Class<EnginePoint> getTypeReference() {
        return EnginePoint.class;
    }

    @Override
    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {
        // TODO!
    }

}
