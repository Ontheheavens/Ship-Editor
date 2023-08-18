package oth.shipeditor.components.viewer.painters.points;

import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public class EngineSlotPainter extends AngledPointPainter{

    EngineSlotPainter(ShipPainter parent) {
        super(parent);
    }

    @Override
    public List<? extends BaseWorldPoint> getPointsIndex() {
        return null;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {

    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {

    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        return 0;
    }

    @Override
    protected Class<? extends BaseWorldPoint> getTypeReference() {
        return null;
    }

    @Override
    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {

    }

}
