package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.EngineAngleChangeQueued;
import oth.shipeditor.communication.events.viewer.points.EnginePointsSorted;
import oth.shipeditor.communication.events.viewer.points.EngineSizeChangeQueued;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Size2D;

import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
public class EngineSlotPainter extends AngledPointPainter {

    @Getter
    private boolean controlHotkeyPressed;

    @Getter @Setter
    private boolean creationHotkeyPressed;

    @Getter
    private static boolean controlHotkeyStaticPressed;

    @Getter @Setter
    private List<EnginePoint> enginePoints;

    public EngineSlotPainter(ShipPainter parent) {
        super(parent);
        this.enginePoints = new ArrayList<>();
        this.initPointListening();
    }

    @Override
    protected void handlePointSelectionEvent(BaseWorldPoint point) {
        if (controlHotkeyStaticPressed) return;
        super.handlePointSelectionEvent(point);
    }

    @Override
    public void setControlHotkeyPressed(boolean pressed) {
        this.controlHotkeyPressed = pressed;
        controlHotkeyStaticPressed = pressed;
    }

    @Override
    protected int getControlHotkey() {
        return KeyEvent.VK_D;
    }

    @Override
    protected int getCreationHotkey() {
        return KeyEvent.VK_E;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initPointListening() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener controlListener = event -> {
            if (event instanceof EngineAngleChangeQueued checked) {
                if (!isInteractionEnabled() || !isControlHotkeyPressed()) return;
                super.changePointAngleByTarget(checked.worldTarget());
            } else if (event instanceof EngineSizeChangeQueued checked) {
                if (!isInteractionEnabled() || !isControlHotkeyPressed()) return;
                this.handleSizeChange(checked.worldTarget());
            }
        };
        listeners.add(controlListener);
        EventBus.subscribe(controlListener);
        BusEventListener enginesSortingListener = event -> {
            if (event instanceof EnginePointsSorted checked) {
                if (!isInteractionEnabled()) return;
                EditDispatch.postEnginesRearranged(this, this.enginePoints, checked.rearranged());
            }
        };
        listeners.add(enginesSortingListener);
        EventBus.subscribe(enginesSortingListener);
    }

    private void handleSizeChange(Point2D worldTarget) {
        WorldPoint selected = this.getSelected();
        if (selected instanceof EnginePoint checked) {
            Point2D position = checked.getPosition();

            AffineTransform rotateInstance = AffineTransform.getRotateInstance(Math.toRadians(checked.getAngle()),
                    position.getX(), position.getY());
            Point2D rotatedTarget = rotateInstance.transform(worldTarget, null);

            double halfWidth = Math.abs(rotatedTarget.getX() - position.getX());
            double length = Math.abs(rotatedTarget.getY() - position.getY());

            if (ControlPredicates.isCursorSnappingEnabled()) {
                length = Math.round(length * 2.0d) / 2.0d;
                halfWidth = Math.round(halfWidth * 2.0d) / 2.0d;
            }

            double fullWidth = halfWidth * 2.0d;
            this.changeEngineSizeWithMirrorCheck(checked, new Size2D(fullWidth, length));
        } else if (selected !=null) {
            throwIllegalPoint();
        }
    }

    public void changeEngineSizeWithMirrorCheck(EnginePoint point, Size2D size) {
        point.changeSize(size);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(point);
        if (mirrorMode && mirroredCounterpart instanceof EnginePoint checkedCounterpart) {
            checkedCounterpart.changeSize(size);
        }
    }

    public void changeEngineContrailWithMirrorCheck(EnginePoint point, int contrailSize) {
        point.setContrailSize(contrailSize);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(point);
        if (mirrorMode && mirroredCounterpart instanceof EnginePoint checkedCounterpart) {
            checkedCounterpart.setContrailSize(contrailSize);
        }
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
