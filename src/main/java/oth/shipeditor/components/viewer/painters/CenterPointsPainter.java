package oth.shipeditor.components.viewer.painters;

import oth.shipeditor.components.viewer.entities.BaseWorldPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Also intended to handle collision and shield radii and their painting.
 * @author Ontheheavens
 * @since 09.06.2023
 */
public class CenterPointsPainter extends AbstractPointPainter {

    private final List<BaseWorldPoint> points = new ArrayList<>();

    @Override
    public List<BaseWorldPoint> getPointsIndex() {
        return points;
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