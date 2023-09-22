package oth.shipeditor.components.viewer.painters.points.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.MarkPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;

import java.util.ArrayList;
import java.util.List;

/**
 * Meant to be used to place freeform points, for measuring or visual help.
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public final class MarkPointsPainter extends AbstractPointPainter {

    @Getter
    private final List<MarkPoint> markPoints;

    private MarkPointsPainter() {
        this.markPoints = new ArrayList<>();
        this.initCreationListener();
        this.setInteractionEnabled(false);
    }

    private void initCreationListener() {
        EventBus.subscribe(event -> {
            if (event instanceof PointCreationQueued checked) {
                if (!isInteractionEnabled()) return;
                this.addPoint(new BaseWorldPoint(checked.position()));
            }
        });
    }

    /**
     * @return true, because World Points are always considered active.
     */
    @Override
    protected boolean isParentLayerActive() {
        return true;
    }

    @Override
    public boolean isMirrorable() {
        return false;
    }

    /**
     * @return instance of the painter from factory.
     */
    public static MarkPointsPainter create() {
        return new MarkPointsPainter();
    }

    @Override
    public List<MarkPoint> getPointsIndex() {
        return markPoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof MarkPoint checked) {
            markPoints.add(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof MarkPoint checked) {
            markPoints.remove(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    public BaseWorldPoint getMirroredCounterpart(WorldPoint inputPoint) {
        return null;
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof MarkPoint checked) {
            return markPoints.indexOf(checked);
        } else {
            throwIllegalPoint();
            return -1;
        }
    }

    @Override
    protected Class<MarkPoint> getTypeReference() {
        return MarkPoint.class;
    }

}
