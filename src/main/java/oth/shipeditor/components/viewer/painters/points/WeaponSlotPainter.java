package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.instrument.ship.slots.SlotCreationPane;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotDrawingHelper;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;

/**
 * Is not supposed to handle launch bays - bays deserialize to different points and painter.
 * @author Ontheheavens
 * @since 25.07.2023
 */
@SuppressWarnings({"OverlyComplexClass", "OverlyCoupledClass"})
@Log4j2
public class WeaponSlotPainter extends MirrorablePointPainter{

    private static final String ILLEGAL_POINT_TYPE_FOUND_IN_WEAPON_SLOT_PAINTER = "Illegal point type found in WeaponSlotPainter!";
    private static final char SPACE = ' ';

    @Getter @Setter
    private List<WeaponSlotPoint> slotPoints;

    private final int controlHotkey = KeyEvent.VK_A;

    private final int creationHotkey = KeyEvent.VK_W;

    @Getter
    private static boolean controlHotkeyPressed;

    @Getter
    private static boolean creationHotkeyPressed;

    private KeyEventDispatcher hotkeyDispatcher;

    private final SlotDrawingHelper slotMockDrawer = new SlotDrawingHelper(null);

    private final SlotDrawingHelper counterpartMockDrawer = new SlotDrawingHelper(null);

    public WeaponSlotPainter(ShipPainter parent) {
        super(parent);
        this.slotPoints = new ArrayList<>();

        this.initHotkeys();
        this.initModeListener();
        this.initInteractionListeners();

        this.setInteractionEnabled(ShipInstrumentsPane.getCurrentMode() == ShipInstrument.WEAPON_SLOTS);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initInteractionListeners() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener slotCreationListener = event -> {
            if (event instanceof SlotCreationQueued checked) {
                if (!isInteractionEnabled()) return;
                if (!hasPointAtCoords(checked.position())) {
                    this.createSlot(checked);
                }
            }
        };
        listeners.add(slotCreationListener);
        EventBus.subscribe(slotCreationListener);
        BusEventListener controlListener = event -> {
            if (event instanceof SlotAngleChangeQueued checked) {
                if (!isInteractionEnabled() || !controlHotkeyPressed) return;
                this.changeAngleByTarget(checked.worldTarget());
            }
            else if (event instanceof SlotArcChangeQueued checked) {
                if (!isInteractionEnabled() || !controlHotkeyPressed) return;
                this.changeArcByTarget(checked.worldTarget());
            }
        };
        listeners.add(controlListener);
        EventBus.subscribe(controlListener);
        BusEventListener slotSortingListener = event -> {
            if (event instanceof SlotPointsSorted checked) {
                if (!isInteractionEnabled()) return;
                EditDispatch.postSlotsRearranged(this, this.slotPoints, checked.rearranged());
            }
        };
        listeners.add(slotSortingListener);
        EventBus.subscribe(slotSortingListener);
    }

    private void createSlot(SlotCreationQueued event) {
        if (!creationHotkeyPressed) return;

        ShipPainter parentLayer = this.getParentLayer();
        Point2D position = event.position();
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();

        WeaponSlotPoint created = null;
        WeaponSlotPoint counterpart = null;

        String uniqueID = this.generateUniqueID();

        switch (SlotCreationPane.getMode()) {
            case BY_CLOSEST -> {
                WeaponSlotPoint closest = (WeaponSlotPoint) findClosestPoint(position);
                created = new WeaponSlotPoint(position, parentLayer, closest);
                created.setId(uniqueID);
            }
            case BY_DEFAULT -> {
                created = new WeaponSlotPoint(position, parentLayer);
                created.setId(uniqueID);
                created.setWeaponType(SlotCreationPane.getDefaultType());
                created.setWeaponMount(SlotCreationPane.getDefaultMount());
                created.setWeaponSize(SlotCreationPane.getDefaultSize());
                created.setAngle(SlotCreationPane.getDefaultAngle());
                created.setArc(SlotCreationPane.getDefaultArc());
            }
        }

        if (mirrorMode) {
            if (getMirroredCounterpart(created) == null) {
                Point2D counterpartPosition = createCounterpartPosition(position);
                counterpart = new WeaponSlotPoint(counterpartPosition, parentLayer, created);
                String incrementedID = this.incrementUniqueID(uniqueID);
                counterpart.setId(incrementedID);
                double flipAngle = Utility.flipAngle(counterpart.getAngle());
                counterpart.setAngle(flipAngle);
            }
        }

        EditDispatch.postPointAdded(this, created);
        if (counterpart != null) {
            EditDispatch.postPointAdded(this, counterpart);
        }
    }

    private String generateUniqueID() {
        ShipPainter parentLayer = this.getParentLayer();
        Set<String> existingIDs = parentLayer.getAllSlotIDs();

        String baseID = "WS";
        int suffix = 0;

        while (true) {
            String newID = baseID + " " + String.format("%03d", suffix);
            if (!existingIDs.contains(newID)) {
                return newID;
            }
            suffix++;
        }
    }

    private String incrementUniqueID(String id) {
        ShipPainter parentLayer = this.getParentLayer();
        Set<String> existingIDs = parentLayer.getAllSlotIDs();

        String baseID = id.substring(0, id.lastIndexOf(SPACE) + 1);
        int suffix = Integer.parseInt(id.substring(id.lastIndexOf(SPACE) + 1));

        while (true) {
            suffix++;
            String newID = baseID + String.format("%03d", suffix);
            if (!existingIDs.contains(newID)) {
                return newID;
            }
        }
    }

    private Set<WeaponSlotPoint> getSlotsWithCounterparts(Iterable<WeaponSlotPoint> slots) {
        Set<WeaponSlotPoint> resultSet = new HashSet<>();
        for (WeaponSlotPoint point : slots) {
            resultSet.add(point);

            boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
            BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(point);
            if (mirrorMode && mirroredCounterpart instanceof WeaponSlotPoint checkedSlot) {
                resultSet.add(checkedSlot);
            }
        }
        return resultSet;
    }

    public void changeSlotsTypeWithMirrorCheck(WeaponType inputType, Iterable<WeaponSlotPoint> slots) {
        Collection<WeaponSlotPoint> slotsWithCounterparts = this.getSlotsWithCounterparts(slots);
        for (WeaponSlotPoint slot : slotsWithCounterparts) {
            slot.changeSlotType(inputType);
        }
    }

    public void changeSlotsMountWithMirrorCheck(WeaponMount inputMount, Iterable<WeaponSlotPoint> slots) {
        Collection<WeaponSlotPoint> slotsWithCounterparts = this.getSlotsWithCounterparts(slots);
        for (WeaponSlotPoint slot : slotsWithCounterparts) {
            slot.changeSlotMount(inputMount);
        }
    }

    public void changeSlotsSizeWithMirrorCheck(WeaponSize inputSize, Iterable<WeaponSlotPoint> slots) {
        Collection<WeaponSlotPoint> slotsWithCounterparts = this.getSlotsWithCounterparts(slots);
        for (WeaponSlotPoint slot : slotsWithCounterparts) {
            slot.changeSlotSize(inputSize);
        }
    }

    private void changeArcByTarget(Point2D worldTarget) {
        WorldPoint selected = getSelected();
        if (!(selected instanceof WeaponSlotPoint checked)) {
            throw new IllegalArgumentException(ILLEGAL_POINT_TYPE_FOUND_IN_WEAPON_SLOT_PAINTER);
        }
        double directionAngle = checked.getAngle();
        double targetAngle = WeaponSlotPainter.getTargetRotation(checked, worldTarget);

        double angleDifference = targetAngle - directionAngle;

        // Normalize the angle difference to the range from -180 to 180 degrees.
        if (angleDifference > 180) {
            angleDifference -= 360;
        } else if (angleDifference < -180) {
            angleDifference += 360;
        }

        // Calculate the arc extent based on the normalized angle difference.
        double arcExtent = Math.abs(angleDifference) * 2;

        this.changeArcWithMirrorCheck(checked, arcExtent);
    }

    public void changeArcWithMirrorCheck(WeaponSlotPoint slotPoint, double arcExtentDegrees) {
        slotPoint.changeSlotArc(arcExtentDegrees);

        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(slotPoint);
        if (mirrorMode && mirroredCounterpart instanceof WeaponSlotPoint checkedSlot) {
            checkedSlot.changeSlotArc(arcExtentDegrees);
        }
    }

    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {
        if (!(toInsert instanceof WeaponSlotPoint checked)) {
            throw new IllegalStateException("Attempted to insert incompatible point to WeaponSlotPainter!");
        }
        slotPoints.add(precedingIndex, checked);
        EventBus.publish(new WeaponSlotInsertedConfirmed(checked, precedingIndex));
        log.info("Weapon slot inserted to painter: {}", checked);
    }

    private static double getTargetRotation(WorldPoint selected, Point2D worldTarget) {
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

    private void changeAngleByTarget(Point2D worldTarget) {
        WorldPoint selected = getSelected();
        if (!(selected instanceof WeaponSlotPoint checked)) {
            throw new IllegalArgumentException(ILLEGAL_POINT_TYPE_FOUND_IN_WEAPON_SLOT_PAINTER);
        }
        double result = WeaponSlotPainter.getTargetRotation(checked, worldTarget);
        this.changeAngleWithMirrorCheck(checked, result);
    }

    public void changeAngleWithMirrorCheck(WeaponSlotPoint slotPoint, double angleDegrees) {
        slotPoint.changeSlotAngle(angleDegrees);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(slotPoint);
        if (mirrorMode && mirroredCounterpart instanceof WeaponSlotPoint checkedSlot) {
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

    @Override
    public void cleanupListeners() {
        super.cleanupListeners();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(hotkeyDispatcher);
    }

    private void initHotkeys() {
        hotkeyDispatcher = ke -> {
            int keyCode = ke.getKeyCode();
            boolean isControlHotkey = (keyCode == controlHotkey);
            boolean isCreationHotkey = (keyCode == creationHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isControlHotkey) {
                        controlHotkeyPressed = true;
                        EventBus.publish(new ViewerRepaintQueued());
                    } else if (isCreationHotkey) {
                        creationHotkeyPressed = true;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isControlHotkey) {
                        controlHotkeyPressed = false;
                        EventBus.publish(new ViewerRepaintQueued());
                    } else if (isCreationHotkey) {
                        creationHotkeyPressed = false;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
            }
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

    public void toggleSkinSlotOverride(ShipSkin skin) {
        this.slotPoints.forEach(weaponSlotPoint -> WeaponSlotPainter.setSlotOverrideFromSkin(weaponSlotPoint, skin));
    }

    public static void setSlotOverrideFromSkin(WeaponSlotPoint weaponSlotPoint, ShipSkin skin) {
        if (skin == null || skin.isBase()) {
            weaponSlotPoint.setSkinOverride(null);
            return;
        }
        String slotID = weaponSlotPoint.getId();
        Map<String, WeaponSlotOverride> weaponSlotChanges = skin.getWeaponSlotChanges();
        WeaponSlotOverride matchingOverride = weaponSlotChanges.get(slotID);
        weaponSlotPoint.setSkinOverride(matchingOverride);
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
    void paintDelegates(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        super.paintDelegates(g, worldToScreen, w, h);
        for (WeaponSlotPoint point : getPointsIndex()) {
            WeaponSlotPainter.setSlotTransparency(point, 0.8d);
        }
    }

    @Override
    protected void handleSelectionHighlight() {
        WorldPoint selection = this.getSelected();
        double full = 1.0d;
        if (selection != null && isInteractionEnabled()) {
            WeaponSlotPainter.setSlotTransparency(selection, full);
            WorldPoint counterpart = this.getMirroredCounterpart(selection);
            if (counterpart != null && ControlPredicates.isMirrorModeEnabled()) {
                WeaponSlotPainter.setSlotTransparency(counterpart, full);
            }
        }
    }

    @Override
    public void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        super.paintPainterContent(g, worldToScreen, w, h);

        if (isInteractionEnabled() && creationHotkeyPressed) {
            Point2D finalWorldCursor = StaticController.getFinalWorldCursor();
            Point2D worldCounterpart = this.createCounterpartPosition(finalWorldCursor);
            boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();

            switch (SlotCreationPane.getMode()) {
                case BY_CLOSEST -> {
                    WeaponSlotPoint closest = (WeaponSlotPoint) findClosestPoint(finalWorldCursor);
                    slotMockDrawer.setType(closest.getWeaponType());
                    slotMockDrawer.setMount(closest.getWeaponMount());
                    slotMockDrawer.setSize(closest.getWeaponSize());
                    slotMockDrawer.setAngle(closest.getAngle());
                    slotMockDrawer.setArc(closest.getArc());
                }
                case BY_DEFAULT -> {
                    slotMockDrawer.setType(SlotCreationPane.getDefaultType());
                    slotMockDrawer.setMount(SlotCreationPane.getDefaultMount());
                    slotMockDrawer.setSize(SlotCreationPane.getDefaultSize());
                    slotMockDrawer.setAngle(SlotCreationPane.getDefaultAngle());
                    slotMockDrawer.setArc(SlotCreationPane.getDefaultArc());
                }
            }

            slotMockDrawer.setPointPosition(finalWorldCursor);
            slotMockDrawer.paintSlotVisuals(g, worldToScreen);
            if (mirrorMode) {
                counterpartMockDrawer.setType(slotMockDrawer.getType());
                counterpartMockDrawer.setMount(slotMockDrawer.getMount());
                counterpartMockDrawer.setSize(slotMockDrawer.getSize());

                double flipAngle = Utility.flipAngle(slotMockDrawer.getAngle());
                counterpartMockDrawer.setAngle(flipAngle);
                counterpartMockDrawer.setArc(slotMockDrawer.getArc());

                counterpartMockDrawer.setPointPosition(worldCounterpart);
                counterpartMockDrawer.paintSlotVisuals(g, worldToScreen);
            }
        }
    }

    private static void setSlotTransparency(WorldPoint point, double value) {
        if (point instanceof WeaponSlotPoint checked) {
            checked.setTransparency(value);
        } else {
            throw new IllegalStateException(ILLEGAL_POINT_TYPE_FOUND_IN_WEAPON_SLOT_PAINTER);
        }
    }

    @Override
    protected Class<WeaponSlotPoint> getTypeReference() {
        return WeaponSlotPoint.class;
    }

}
