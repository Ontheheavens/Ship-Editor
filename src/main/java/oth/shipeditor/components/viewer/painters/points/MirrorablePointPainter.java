package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
public abstract class MirrorablePointPainter extends AbstractPointPainter {

    @Getter
    private final ShipPainter parentLayer;

    MirrorablePointPainter(ShipPainter parent) {
        this.parentLayer = parent;
    }

    @Override
    protected boolean isParentLayerActive() {
        return this.parentLayer.isLayerActive();
    }

    @Override
    protected Point2D createCounterpartPosition(Point2D toMirror) {
        ShipCenterPoint shipCenter = parentLayer.getShipCenter();
        Point2D centerPosition = shipCenter.getPosition();
        double counterpartX = 2 * centerPosition.getX() - toMirror.getX();
        double counterpartY = toMirror.getY(); // Y-coordinate remains the same.
        return new Point2D.Double(counterpartX, counterpartY);
    }

    @Override
    protected boolean checkVisibility() {
        PainterVisibility visibilityMode = getVisibilityMode();
        boolean parentCheck = super.checkVisibility();
        if (visibilityMode == PainterVisibility.SHOWN_WHEN_SELECTED && !parentLayer.isLayerActive()) return false;
        return parentCheck;
    }

    @Override
    public boolean isMirrorable() {
        return true;
    }

    @Override
    public BaseWorldPoint getMirroredCounterpart(WorldPoint inputPoint) {
        List<? extends BaseWorldPoint> pointsIndex = this.getPointsIndex();
        Point2D pointPosition = inputPoint.getPosition();
        Point2D counterpartPosition = this.createCounterpartPosition(pointPosition);
        double threshold = ControlPredicates.getMirrorPointLinkageTolerance();
        BaseWorldPoint closestPoint = null;
        double closestDistance = Double.MAX_VALUE;
        for (BaseWorldPoint point : pointsIndex) {
            Point2D position = point.getPosition();
            if (position.equals(counterpartPosition)) {
                closestPoint = point;
                return closestPoint;
            }
            double distance = counterpartPosition.distance(position);
            if (distance < closestDistance) {
                closestPoint = point;
                closestDistance = distance;
            }
        }
        if (closestDistance <= threshold) {
            return closestPoint; // Found the mirrored counterpart within the threshold.
        } else {
            return null; // Mirrored counterpart not found.
        }
    }

}
