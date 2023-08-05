package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.SlotAngleChangeQueued;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponSlot;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Is not supposed to handle launch bays - bays deserialize to different points and painter.
 * @author Ontheheavens
 * @since 25.07.2023
 */
public class WeaponSlotPainter extends MirrorablePointPainter{

    @Getter
    private final List<WeaponSlotPoint> slotPoints;

    private final int angleHotkey = KeyEvent.VK_A;

    @Getter
    private static boolean angleHotkeyPressed;

    private KeyEventDispatcher hotkeyDispatcher;

    public WeaponSlotPainter(ShipPainter parent) {
        super(parent);
        this.slotPoints = new ArrayList<>();

        this.initHotkeys();
        this.initModeListener();
        this.initInteractionListeners();

        this.setInteractionEnabled(ShipInstrumentsPane.getCurrentMode() == ShipInstrument.WEAPON_SLOTS);
    }

    private void initInteractionListeners() {
        BusEventListener rotationListener = event -> {
            if (event instanceof SlotAngleChangeQueued checked) {
                if (!isInteractionEnabled() || !angleHotkeyPressed) return;
                this.changeAngleByTarget(checked.worldTarget());
            }
        };
        List<BusEventListener> listeners = getListeners();
        listeners.add(rotationListener);
        EventBus.subscribe(rotationListener);
    }

    private void changeAngleByTarget(Point2D worldTarget) {
        WorldPoint selected = getSelected();
        if (!(selected instanceof WeaponSlotPoint checked)) {
            throw new IllegalArgumentException("Illegal point type found in WeaponSlotPainter!");
        }
        Point2D pointPosition = checked.getPosition();
        double deltaX = worldTarget.getX() - pointPosition.getX();
        double deltaY = worldTarget.getY() - pointPosition.getY();

        double radians = Math.atan2(deltaX, deltaY);

        double rotationDegrees = Math.toDegrees(radians) + 180;
        double result = rotationDegrees;
        if (ControlPredicates.isRotationRoundingEnabled()) {
            result = Math.round(rotationDegrees);
        }
        checked.changeSlotAngle(result);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(checked);
        if (mirrorMode && mirroredCounterpart instanceof WeaponSlotPoint checkedSlot) {
            checkedSlot.changeSlotAngle(Utility.flipAngle(result));
        }
    }

    @Override
    public void cleanupListeners() {
        super.cleanupListeners();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(hotkeyDispatcher);
    }

    private void initHotkeys() {
        hotkeyDispatcher = ke -> {
            int keyCode = ke.getKeyCode();
            boolean isAngleHotkey = (keyCode == angleHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isAngleHotkey) {
                        angleHotkeyPressed = true;
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isAngleHotkey) {
                        angleHotkeyPressed = false;
                    }
                    break;
            }
            Events.repaintView();
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(hotkeyDispatcher);
    }

    private void initModeListener() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener modeListener = event -> {
            if (event instanceof InstrumentModeChanged checked) {
                setInteractionEnabled(checked.newMode() == ShipInstrument.WEAPON_SLOTS);
            }
        };
        listeners.add(modeListener);
        EventBus.subscribe(modeListener);
    }

    public void resetSkinSlotOverride() {
        this.slotPoints.forEach(weaponSlotPoint -> weaponSlotPoint.setSkinOverride(null));
    }

    public void toggleSkinSlotOverride(SkinSpecFile skinSpecFile) {
        Collection<WeaponSlotOverride> overrides = new ArrayList<>();
        Map<String, WeaponSlot> weaponSlotChanges = skinSpecFile.getWeaponSlotChanges();
        weaponSlotChanges.forEach((s, weaponSlot) -> {

            // TODO: this is not where instance conversion should happen; move to ShipSkin initialization.

            WeaponSlotOverride.Builder overrideBlueprint = WeaponSlotOverride.Builder.override();
            WeaponType type = WeaponType.value(weaponSlot.getType());
            WeaponSize size = WeaponSize.value(weaponSlot.getSize());
            WeaponMount mount = WeaponMount.value(weaponSlot.getMount());
            WeaponSlotOverride override = overrideBlueprint.withSlotID(s)
                    .withWeaponType(type)
                    .withWeaponSize(size)
                    .withWeaponMount(mount)
                    .build();
            overrides.add(override);
        });
        this.slotPoints.forEach(weaponSlotPoint -> {
            String slotID = weaponSlotPoint.getId();
            WeaponSlotOverride matchingOverride = WeaponSlotPainter.findMatchingOverride(overrides, slotID);
            weaponSlotPoint.setSkinOverride(matchingOverride);
        });
    }

    private static WeaponSlotOverride findMatchingOverride(Iterable<WeaponSlotOverride> overrides, String slotID) {
        for (WeaponSlotOverride override : overrides) {
            if (Objects.equals(override.getSlotID(), slotID)) {
                return override;
            }
        }
        return null;
    }

    @Override
    public List<WeaponSlotPoint> getPointsIndex() {
        return slotPoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            slotPoints.add(checked);
        } else {
            throw new IllegalArgumentException("Attempted to add incompatible point to WeaponSlotPainter!");
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            slotPoints.remove(checked);
        } else {
            throw new IllegalArgumentException("Attempted to remove incompatible point from WeaponSlotPainter!");
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            return slotPoints.indexOf(checked);
        } else {
            throw new IllegalArgumentException("Attempted to access incompatible point in WeaponSlotPainter!");
        }
    }

    @Override
    protected Class<WeaponSlotPoint> getTypeReference() {
        return WeaponSlotPoint.class;
    }

}
