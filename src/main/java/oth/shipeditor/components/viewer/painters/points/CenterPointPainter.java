package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.RadiusDragQueued;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.undo.EditDispatch;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Also intended to handle collision radii and their painting.
 * @author Ontheheavens
 * @since 09.06.2023
 */
@Log4j2
public class CenterPointPainter extends SinglePointPainter {

    public static final float COLLISION_OPACITY = 0.2f;
    private final List<BaseWorldPoint> points = new ArrayList<>();

    @Getter
    private ShipCenterPoint centerPoint;

    private final int dragCollisionRadiusHotkey = KeyEvent.VK_C;

    private boolean collisionRadiusHotkeyPressed;

    private KeyEventDispatcher hotkeyDispatcher;

    public CenterPointPainter(ShipPainter parent) {
        super(parent);
        this.initModeListening();
        this.initHotkeys();
        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.COLLISION);
        this.setPaintOpacity(COLLISION_OPACITY);
    }

    @Override
    public void cleanupListeners() {
        super.cleanupListeners();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(hotkeyDispatcher);
    }

    private void initModeListening() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener modeListener = event -> {
            if (event instanceof InstrumentModeChanged checked) {
                this.setInteractionEnabled(checked.newMode() == InstrumentMode.COLLISION);
                EventBus.publish(new CenterPanelsRepaintQueued());
            }
        };
        listeners.add(modeListener);
        EventBus.subscribe(modeListener);
        BusEventListener radiusDragListener = event -> {
            if (event instanceof RadiusDragQueued checked && isInteractionEnabled()) {
                if (!collisionRadiusHotkeyPressed) return;
                Point2D centerPointPosition = this.centerPoint.getPosition();
                float radius = (float) centerPointPosition.distance(checked.location());
                float result = radius;
                if (ControlPredicates.isCursorSnappingEnabled()) {
                    result = Math.round(radius * 2) / 2.0f;
                }
                EditDispatch.postCollisionRadiusChanged(this.centerPoint, result);
            }
        };
        listeners.add(radiusDragListener);
        EventBus.subscribe(radiusDragListener);
    }

    private void initHotkeys() {
        hotkeyDispatcher = ke -> {
            int keyCode = ke.getKeyCode();
            boolean isCollisionHotkey = (keyCode == dragCollisionRadiusHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isCollisionHotkey) {
                        this.collisionRadiusHotkeyPressed = true;
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isCollisionHotkey) {
                        this.collisionRadiusHotkeyPressed = false;
                    }
                    break;
            }
            EventBus.publish(new ViewerRepaintQueued());
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(hotkeyDispatcher);
    }

    public void initCenterPoint(Point2D translatedCenter, Hull hull) {
        this.centerPoint = new ShipCenterPoint(translatedCenter,
                (float) hull.getCollisionRadius(), this.getParentLayer(), this);
        this.addPoint(centerPoint);
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
    public int getIndexOfPoint(BaseWorldPoint point) {
        return points.indexOf(point);
    }

    @Override
    protected Class<ShipCenterPoint> getTypeReference() {
        return ShipCenterPoint.class;
    }
}