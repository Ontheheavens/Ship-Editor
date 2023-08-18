package oth.shipeditor.components.viewer.painters.points;

import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.Utility;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public abstract class AngledPointPainter extends MirrorablePointPainter {

    AngledPointPainter(ShipPainter parent) {
        super(parent);
    }

    void changeAngleByTarget(Point2D worldTarget) {
        WorldPoint selected = getSelected();
        if (!(selected instanceof AngledPoint checked)) {
            throw new IllegalArgumentException("Illegal point type found in AngledPointPainter!");
        }
        double result = AngledPointPainter.getTargetRotation(checked, worldTarget);
        this.changeAngleWithMirrorCheck(checked, result);
    }

    static double getTargetRotation(WorldPoint selected, Point2D worldTarget) {
        Point2D pointPosition = selected.getPosition();
        double deltaX = worldTarget.getX() - pointPosition.getX();
        double deltaY = worldTarget.getY() - pointPosition.getY();

        double radians = Math.atan2(deltaX, deltaY);

        double rotationDegrees = Math.toDegrees(radians) + 180;
        double result = rotationDegrees;
        if (ControlPredicates.isRotationRoundingEnabled()) {
            result = Math.round(rotationDegrees * 2.0d) / 2.0d;
        }
        return result;
    }

    public void changeAngleWithMirrorCheck(AngledPoint slotPoint, double angleDegrees) {
        slotPoint.changeSlotAngle(angleDegrees);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(slotPoint);
        if (mirrorMode && mirroredCounterpart instanceof AngledPoint checkedSlot) {
            double angle = Utility.flipAngle(angleDegrees);
            Point2D slotPosition = checkedSlot.getPosition();
            double slotX = slotPosition.getX();
            ShipPainter parentLayer = getParentLayer();
            ShipCenterPoint shipCenter = parentLayer.getShipCenter();
            Point2D centerPosition = shipCenter.getPosition();
            double centerX = centerPosition.getX();
            if ((Math.abs(slotX - centerX) < 0.05d)) {
                angle = angleDegrees;
            }
            checkedSlot.changeSlotAngle(angle);
        }
    }

}
