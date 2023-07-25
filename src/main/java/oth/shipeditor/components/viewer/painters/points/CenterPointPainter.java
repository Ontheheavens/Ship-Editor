package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.RadiusDragQueued;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.ApplicationDefaults;

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
public class CenterPointPainter extends AbstractPointPainter {

    private final List<BaseWorldPoint> points = new ArrayList<>();

    @Getter
    private ShipCenterPoint centerPoint;

    private final LayerPainter parentLayer;

    private final int dragCollisionRadiusHotkey = KeyEvent.VK_C;

    private boolean collisionRadiusHotkeyPressed;

    public CenterPointPainter(LayerPainter parent) {
        this.parentLayer = parent;
        this.initModeListening();
        this.initHotkeys();
        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.COLLISION);
        this.setPaintOpacity(ApplicationDefaults.COLLISION_OPACITY);
    }

    private void initModeListening() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                this.setInteractionEnabled(checked.newMode() == InstrumentMode.COLLISION);
                EventBus.publish(new CenterPanelsRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof RadiusDragQueued checked && isInteractionEnabled()) {
                if (!collisionRadiusHotkeyPressed) return;
                Point2D centerPointPosition = this.centerPoint.getPosition();
                float radius = (float) centerPointPosition.distance(checked.location());
                float rounded = Math.round(radius * 2) / 2.0f;
                EditDispatch.postCollisionRadiusChanged(this.centerPoint, rounded);
            }
        });
    }

    @Override
    protected boolean isParentLayerActive() {
        return this.parentLayer.isLayerActive();
    }

    private void initHotkeys() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
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
        });
    }

    @Override
    public boolean isMirrorable() {
        return false;
    }

    public void initCenterPoint(Point2D translatedCenter, Hull hull) {
        this.centerPoint = new ShipCenterPoint(translatedCenter,
                (float) hull.getCollisionRadius(), this.parentLayer, this);
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

    /**
     * Conceptually irrelevant for center points.
     * @return null.
     */
    @Override
    public BaseWorldPoint getMirroredCounterpart(WorldPoint inputPoint) {
        throw new UnsupportedOperationException("Mirrored operations unsupported by CenterPointPainters!");
    }

    @Override
    protected Class<ShipCenterPoint> getTypeReference() {
        return ShipCenterPoint.class;
    }
}