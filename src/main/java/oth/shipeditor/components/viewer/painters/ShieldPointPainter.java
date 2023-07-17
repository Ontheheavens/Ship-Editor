package oth.shipeditor.components.viewer.painters;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CentersPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.RadiusDragQueued;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.instrument.centers.CenterPointMode;
import oth.shipeditor.components.instrument.centers.HullPointsPanel;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.ApplicationDefaults;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.CENTERS);
    }

    public void initShieldPoint(Point2D translated, Hull hull) {
        String styleID = hull.getStyle();
        GameDataRepository dataRepository = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = dataRepository.getAllHullStyles();
        HullStyle hullStyle = allHullStyles.get(styleID);
        this.shieldCenterPoint = new ShieldCenterPoint(translated,
                (float) hull.getShieldRadius(), this.parentLayer, hullStyle, this);
        this.addPoint(shieldCenterPoint);
        Color shieldInnerColor = hullStyle.getShieldInnerColor();
        float styleInnerColorOpacity = Utility.getOpacityFromAlpha(shieldInnerColor.getAlpha());
        this.setPaintOpacity(styleInnerColorOpacity);
    }

    private void initModeListening() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                this.setInteractionEnabled(checked.newMode() == InstrumentMode.CENTERS);
                EventBus.publish(new CentersPanelRepaintQueued());
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
    public boolean isInteractionEnabled() {
        boolean basicCheck = super.isInteractionEnabled() && this.parentLayer.isLayerActive();
        return basicCheck && HullPointsPanel.getMode() == CenterPointMode.SHIELD;
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
    public WorldPoint getMirroredCounterpart(WorldPoint point) {
        throw new UnsupportedOperationException("Mirrored operations unsupported by ShieldPointPainters!");
    }

    @Override
    protected BaseWorldPoint getTypeReference() {
        return new BaseWorldPoint();
    }

}
