package oth.shipeditor.components.viewer.painters;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Meant to be used to place freeform points, for measuring or visual help.
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public final class WorldPointsPainter extends AbstractPointPainter {

    @Getter
    private final List<BaseWorldPoint> worldPoints;

    private WorldPointsPainter() {
        this.worldPoints = new ArrayList<>();
    }

    /**
     * @return instance of the painter from factory.
     */
    public static WorldPointsPainter create() {
        return new WorldPointsPainter();
    }

    @Override
    protected List<BaseWorldPoint> getPointsIndex() {
        return worldPoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        worldPoints.add(point);
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        worldPoints.remove(point);
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        return worldPoints.indexOf(point);
    }

    @Override
    protected BaseWorldPoint getTypeReference() {
        return new BaseWorldPoint();
    }

}
