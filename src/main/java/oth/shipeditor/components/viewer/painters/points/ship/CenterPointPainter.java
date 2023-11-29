package oth.shipeditor.components.viewer.painters.points.ship;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.RadiusDragQueued;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.TextPainter;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
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

    private static final float COLLISION_OPACITY = 0.2f;
    private final List<BaseWorldPoint> points = new ArrayList<>();

    @Getter
    private ShipCenterPoint centerPoint;

    @Getter @Setter
    private Point2D moduleAnchorOffset;

    private final TextPainter moduleAnchorText;

    private static final int dragCollisionRadiusHotkey = KeyEvent.VK_C;

    private boolean collisionRadiusHotkeyPressed;

    private KeyEventDispatcher hotkeyDispatcher;

    public CenterPointPainter(ShipPainter parent) {
        super(parent);
        this.initModeListening();
        this.initHotkeys();
        this.setInteractionEnabled(StaticController.getEditorMode() == EditorInstrument.COLLISION);
        this.setPaintOpacity(COLLISION_OPACITY);

        this.moduleAnchorText = new TextPainter();
    }

    public void changeModuleAnchor(Point2D updated) {
        EditDispatch.postModuleAnchorChanged(this, updated);
        EditDispatch.notifyTimedEditCommenced();
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
                EditorInstrument editorInstrument = EditorInstrument.COLLISION;
                this.setInteractionEnabled(checked.newMode() == editorInstrument);
                EventBus.publish(new InstrumentRepaintQueued(editorInstrument));
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
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isCollisionHotkey) {
                        this.collisionRadiusHotkeyPressed = false;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(hotkeyDispatcher);
    }

    public void initCenterPoint(Point2D translatedCenter, HullSpecFile hullSpecFile) {
        if (this.centerPoint != null) {
            this.removePoint(centerPoint);
        }
        this.centerPoint = new ShipCenterPoint(translatedCenter,
                (float) hullSpecFile.getCollisionRadius(), this.getParentLayer(), this);
        this.addPoint(centerPoint);
    }

    @Override
    void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (moduleAnchorOffset == null) return;

        Composite old = null;
        if (this.getPaintOpacity() != 0.0f) {
            old = Utility.setFullAlpha(g);
        }

        Point2D centerPosition = this.centerPoint.getPosition();
        double x = centerPosition.getX() - moduleAnchorOffset.getY();
        double y = centerPosition.getY() - moduleAnchorOffset.getX();
        Point2D resultAnchorLocation = new Point2D.Double(x, y);
        Color moduleColor = WeaponType.STATION_MODULE.getColor();
        DrawUtilities.drawEntityCenterCross(g, worldToScreen, resultAnchorLocation, moduleColor);

        Point2D toDisplay = Utility.getPointCoordinatesForDisplay(resultAnchorLocation);

        DrawUtilities.drawWithConditionalOpacity(g, graphics2D -> {
            String coords = StringValues.MODULE_ANCHOR + " (" + toDisplay.getX() + ", " + toDisplay.getY() + ")";

            moduleAnchorText.setWorldPosition(resultAnchorLocation);
            moduleAnchorText.setText(coords);
            moduleAnchorText.paintText(graphics2D, worldToScreen);
        });

        if (old != null) {
            g.setComposite(old);
        }
    }

    @Override
    public List<BaseWorldPoint> getPointsIndex() {
        return points;
    }

    @Override
    public void removePoint(BaseWorldPoint point) {
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