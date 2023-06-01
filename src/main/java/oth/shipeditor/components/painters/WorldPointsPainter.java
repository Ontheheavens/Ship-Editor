package oth.shipeditor.components.painters;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.entities.BaseWorldPoint;
import oth.shipeditor.components.entities.WorldPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Meant to be used to place freeform points, for measuring or visual help.
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public final class WorldPointsPainter extends AbstractPointPainter {

    private WorldPointsPainter() {
    }

    /**
     * @return instance of the painter from factory.
     */
    public static WorldPointsPainter create() {
        return new WorldPointsPainter();
    }

    @Override
    protected List<BaseWorldPoint> getPointsIndex() {
        int size = this.pointsIndex.size();
        List<BaseWorldPoint> allPoints = new ArrayList<>(size);
        for (WorldPoint retrieved : this.pointsIndex) {
            allPoints.add((BaseWorldPoint) retrieved);
        }
        return allPoints;
    }

    @Override
    protected BaseWorldPoint getTypeReference() {
        return new BaseWorldPoint();
    }

}
