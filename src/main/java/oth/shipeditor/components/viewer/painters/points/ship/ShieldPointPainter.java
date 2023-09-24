package oth.shipeditor.components.viewer.painters.points.ship;

import lombok.Getter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.RadiusDragQueued;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;
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
public class ShieldPointPainter extends SinglePointPainter {

    private final List<BaseWorldPoint> points = new ArrayList<>();

    @Getter
    private ShieldCenterPoint shieldCenterPoint;

    private final int dragShieldRadiusHotkey = KeyEvent.VK_S;

    private boolean shieldRadiusHotkeyPressed;

    private KeyEventDispatcher hotkeyDispatcher;

    public ShieldPointPainter(ShipPainter parent) {
        super(parent);
        this.initModeListening();
        this.initHotkeys();
        this.setInteractionEnabled(StaticController.getEditorMode() == EditorInstrument.SHIELD);
    }

    @Override
    public void cleanupListeners() {
        super.cleanupListeners();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(hotkeyDispatcher);
    }

    public void initShieldPoint(Point2D translated, HullSpecFile hullSpecFile) {
        HullStyle style = GameDataRepository.fetchStyleByID(hullSpecFile.getStyle());
        if (style == null) {
            style = new HullStyle();
        }
        this.shieldCenterPoint = new ShieldCenterPoint(translated,
                (float) hullSpecFile.getShieldRadius(), this.getParentLayer(), style, this);
        this.addPoint(shieldCenterPoint);
        Color shieldInnerColor = style.getShieldInnerColor();
        float styleInnerColorOpacity = ColorUtilities.getOpacityFromAlpha(shieldInnerColor.getAlpha());
        this.setPaintOpacity(styleInnerColorOpacity);
    }

    private void initModeListening() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener modeListener = event -> {
            if (event instanceof InstrumentModeChanged checked) {
                this.setInteractionEnabled(checked.newMode() == EditorInstrument.SHIELD);
                EventBus.publish(new CenterPanelsRepaintQueued());
            }
        };
        listeners.add(modeListener);
        EventBus.subscribe(modeListener);
        BusEventListener radiusDragListener = event -> {
            if (event instanceof RadiusDragQueued checked && isInteractionEnabled()) {
                if (!shieldRadiusHotkeyPressed) return;
                Point2D pointPosition = this.shieldCenterPoint.getPosition();
                float radius = (float) pointPosition.distance(checked.location());
                float result = radius;
                if (ControlPredicates.isCursorSnappingEnabled()) {
                    result = Math.round(radius * 2) / 2.0f;
                }
                EditDispatch.postShieldRadiusChanged(this.shieldCenterPoint, result);
            }
        };
        listeners.add(radiusDragListener);
        EventBus.subscribe(radiusDragListener);
    }

    private void initHotkeys() {
        hotkeyDispatcher = ke -> {
            int keyCode = ke.getKeyCode();
            boolean isShieldHotkey = (keyCode == dragShieldRadiusHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isShieldHotkey) {
                        this.shieldRadiusHotkeyPressed = true;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isShieldHotkey) {
                        this.shieldRadiusHotkeyPressed = false;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(hotkeyDispatcher);
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
    protected Class<ShieldCenterPoint> getTypeReference() {
        return ShieldCenterPoint.class;
    }

}
