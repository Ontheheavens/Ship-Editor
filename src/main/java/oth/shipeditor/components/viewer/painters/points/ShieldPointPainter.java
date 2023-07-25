package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.RadiusDragQueued;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.graphics.ColorUtilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public class ShieldPointPainter extends AbstractPointPainter{

    private final List<BaseWorldPoint> points = new ArrayList<>();

    @Getter
    private ShieldCenterPoint shieldCenterPoint;

    private final LayerPainter parentLayer;

    private final int dragShieldRadiusHotkey = KeyEvent.VK_S;

    private boolean shieldRadiusHotkeyPressed;

    public ShieldPointPainter(LayerPainter parent) {
        this.parentLayer = parent;
        this.initModeListening();
        this.initHotkeys();
        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.SHIELD);
    }

    public void initShieldPoint(Point2D translated, ShipData data) {
        Hull hull = data.getHull();
        HullStyle style = data.getHullStyle();
        if (style == null) {
            style = new HullStyle();
        }
        this.shieldCenterPoint = new ShieldCenterPoint(translated,
                (float) hull.getShieldRadius(), this.parentLayer, style, this);
        this.addPoint(shieldCenterPoint);
        Color shieldInnerColor = style.getShieldInnerColor();
        float styleInnerColorOpacity = ColorUtilities.getOpacityFromAlpha(shieldInnerColor.getAlpha());
        this.setPaintOpacity(styleInnerColorOpacity);
    }

    private void initModeListening() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                this.setInteractionEnabled(checked.newMode() == InstrumentMode.SHIELD);
                EventBus.publish(new CenterPanelsRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof RadiusDragQueued checked && isInteractionEnabled()) {
                if (!shieldRadiusHotkeyPressed) return;
                Point2D pointPosition = this.shieldCenterPoint.getPosition();
                float radius = (float) pointPosition.distance(checked.location());
                float rounded = Math.round(radius * 2) / 2.0f;
                EditDispatch.postShieldRadiusChanged(this.shieldCenterPoint, rounded);
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
            boolean isShieldHotkey = (keyCode == dragShieldRadiusHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isShieldHotkey) {
                        this.shieldRadiusHotkeyPressed = true;
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isShieldHotkey) {
                        this.shieldRadiusHotkeyPressed = false;
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
    public int getIndexOfPoint(BaseWorldPoint point) {
        return points.indexOf(point);
    }

    @Override
    public WorldPoint getMirroredCounterpart(WorldPoint inputPoint) {
        throw new UnsupportedOperationException("Mirrored operations unsupported by ShieldPointPainters!");
    }

    @Override
    protected Class<ShieldCenterPoint> getTypeReference() {
        return ShieldCenterPoint.class;
    }

}
