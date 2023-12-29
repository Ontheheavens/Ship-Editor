package oth.shipeditor.components.viewer.painters.points.ship;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.slots.SlotCreationPane;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.SlotDrawer;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.points.AngledPointPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;

import static oth.shipeditor.components.viewer.painters.PainterVisibility.ALWAYS_SHOWN;
import static oth.shipeditor.components.viewer.painters.PainterVisibility.SHOWN_WHEN_EDITED;

/**
 * Is not supposed to handle launch bays - bays deserialize to different points and painter.
 * @author Ontheheavens
 * @since 25.07.2023
 */
@SuppressWarnings({"OverlyCoupledClass", "OverlyComplexClass", "ClassWithTooManyMethods"})
@Log4j2
public class WeaponSlotPainter extends AngledPointPainter {

    @Getter
    private boolean controlHotkeyPressed;

    @Getter @Setter
    private boolean creationHotkeyPressed;

    @Getter
    private static boolean controlHotkeyStaticPressed;

    @Getter @Setter
    private List<WeaponSlotPoint> slotPoints;

    private final Map<String, WeaponSlotPoint> slotsByID = new HashMap<>();

    private final SlotDrawer slotMockDrawer = new SlotDrawer(null);

    private final SlotDrawer counterpartMockDrawer = new SlotDrawer(null);

    public WeaponSlotPainter(ShipPainter parent) {
        super(parent);
        this.slotPoints = new ArrayList<>();
    }

    @Override
    public WeaponSlotPoint getSelected() {
        return (WeaponSlotPoint) super.getSelected();
    }

    @Override
    public void setControlHotkeyPressed(boolean pressed) {
        this.controlHotkeyPressed = pressed;
        controlHotkeyStaticPressed = pressed;
    }

    @Override
    protected int getControlHotkey() {
        return KeyEvent.VK_A;
    }

    @Override
    protected int getCreationHotkey() {
        return KeyEvent.VK_W;
    }

    public boolean hasSlotsOfType(WeaponType type) {
        for (WeaponSlotPoint slotPoint : this.getSlotPoints()) {
            WeaponType slotPointType = slotPoint.getWeaponType();
            if (slotPointType == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected EditorInstrument getInstrumentType() {
        return EditorInstrument.WEAPON_SLOTS;
    }

    private void invalidateCaches() {
        this.slotsByID.clear();
    }

    public WeaponSlotPoint getSlotByID(String slotID) {
        WeaponSlotPoint result = this.slotsByID.get(slotID);
        if (result == null) {
            for (WeaponSlotPoint slotPoint : this.slotPoints) {
                String slotPointId = slotPoint.getId();
                if (slotPointId.equals(slotID)) {
                    result = slotPoint;
                }
            }
            this.slotsByID.put(slotID, result);
        }
        return result;
    }

    public boolean isSlotDecorative(String slotID) {
        WeaponSlotPoint slotPoint = this.getSlotByID(slotID);
        return slotPoint != null && slotPoint.isDecorative();
    }

    @Override
    protected void handlePointSelectionEvent(BaseWorldPoint point) {
        if (controlHotkeyStaticPressed) return;
        super.handlePointSelectionEvent(point);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    protected void initInteractionListeners() {
        super.initInteractionListeners();
        List<BusEventListener> listeners = getListeners();
        BusEventListener controlListener = event -> {
            if (event instanceof SlotAngleChangeQueued checked) {
                if (!isInteractionEnabled() || !isControlHotkeyPressed()) return;
                super.changePointAngleByTarget(checked.worldTarget());
            } else if (event instanceof SlotArcChangeQueued checked) {
                if (!isInteractionEnabled() || !isControlHotkeyPressed()) return;
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

    @Override
    public ShipPainter getParentLayer() {
        return (ShipPainter) super.getParentLayer();
    }

    @Override
    protected void handleCreation(PointCreationQueued event) {
        if (!isCreationHotkeyPressed()) return;

        ShipPainter parentLayer = this.getParentLayer();
        Point2D position = event.position();
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();

        WeaponSlotPoint created = null;
        WeaponSlotPoint counterpart = null;

        String uniqueID = this.generateUniqueSlotID();

        switch (SlotCreationPane.getMode()) {
            case BY_CLOSEST -> {
                WeaponSlotPoint closest = (WeaponSlotPoint) findClosestPoint(position);
                if (closest != null) {
                    created = new WeaponSlotPoint(position, parentLayer, closest);
                } else {
                    created = new WeaponSlotPoint(position, parentLayer);
                    created.setWeaponType(SlotCreationPane.getDefaultType());
                    created.setWeaponMount(SlotCreationPane.getDefaultMount());
                    created.setWeaponSize(SlotCreationPane.getDefaultSize());
                    created.setAngle(SlotCreationPane.getDefaultAngle());
                    created.setArc(SlotCreationPane.getDefaultArc());
                }
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
                String incrementedID = parentLayer.incrementUniqueSlotID(uniqueID);
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

    public String generateUniqueSlotID() {
        return this.generateUniqueSlotID("WS");
    }

    private String generateUniqueSlotID(String prefix) {
        ShipPainter parentLayer = getParentLayer();
        return parentLayer.generateUniqueSlotID(prefix);
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

    public void changeSlotsIDWithMirrorCheck(String inputIDText, Iterable<WeaponSlotPoint> slots) {
        Collection<WeaponSlotPoint> slotsWithCounterparts = this.getSlotsWithCounterparts(slots);
        for (WeaponSlotPoint slot : slotsWithCounterparts) {
            String newID = generateUniqueSlotID(inputIDText);
            EditDispatch.postSlotIDChanged(slot, newID);
        }
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
        if (selected instanceof WeaponSlotPoint checked) {
            double directionAngle = checked.getAngle();
            double targetAngle = AngledPointPainter.getTargetRotation(checked, worldTarget);

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
        else {
            throwIllegalPoint();
        }
    }

    public void changeArcWithMirrorCheck(WeaponSlotPoint slotPoint, double arcExtentDegrees) {
        slotPoint.changeSlotArc(arcExtentDegrees);
        actOnCounterpart(point -> point.changeSlotArc(arcExtentDegrees), slotPoint);
    }

    public void changeRenderOrderWithMirrorCheck(WeaponSlotPoint slotPoint, int renderOrder) {
        slotPoint.changeRenderOrder(renderOrder);
        actOnCounterpart(point -> point.changeRenderOrder(renderOrder), slotPoint);
    }

    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {
        if (!(toInsert instanceof WeaponSlotPoint checked)) {
            throw new IllegalStateException("Attempted to insert incompatible point to WeaponSlotPainter!");
        }
        slotPoints.add(precedingIndex, checked);
        EventBus.publish(new WeaponSlotInsertedConfirmed(checked, precedingIndex));
        log.trace("Weapon slot inserted to painter: {}", checked);
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
            invalidateCaches();
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            slotPoints.remove(checked);
            invalidateCaches();
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof WeaponSlotPoint checked) {
            return slotPoints.indexOf(checked);
        } else {
            throwIllegalPoint();
            return -1;
        }
    }

    @Override
    protected void paintDelegates(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        List<WeaponSlotPoint> allPoints = this.getPointsIndex();
        paintSlots(g, worldToScreen, w, h, allPoints);
    }

    private void paintSlots(Graphics2D g, AffineTransform worldToScreen,
                            double w, double h, Iterable<WeaponSlotPoint> slotPointList) {
        slotPointList.forEach(slotPoint -> {
            paintDelegate(g, worldToScreen, w, h, slotPoint);
            slotPoint.setPaintSizeMultiplier(1);
        });
    }

    @Override
    protected void handleSelectionHighlight() {
        WorldPoint selection = this.getSelected();
        double full = 1.5d;
        if (selection != null && isSlotSelectionEnabled()) {
            this.setSlotPaintSize(selection, full);
            WorldPoint counterpart = this.getMirroredCounterpart(selection);
            if (counterpart != null && ControlPredicates.isMirrorModeEnabled()) {
                this.setSlotPaintSize(counterpart, full);
            }
        }
    }

    @Override
    public void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        super.paintPainterContent(g, worldToScreen, w, h);

        if (isInteractionEnabled() && isCreationHotkeyPressed()) {
            Point2D finalWorldCursor = StaticController.getFinalWorldCursor();
            Point2D worldCounterpart = this.createCounterpartPosition(finalWorldCursor);
            boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();

            switch (SlotCreationPane.getMode()) {
                case BY_CLOSEST -> {
                    SlotData closest = (SlotData) findClosestPoint(finalWorldCursor);
                    if (closest != null) {
                        slotMockDrawer.setType(closest.getWeaponType());
                        slotMockDrawer.setMount(closest.getWeaponMount());
                        slotMockDrawer.setSize(closest.getWeaponSize());
                        slotMockDrawer.setAngle(closest.getAngle());
                        slotMockDrawer.setArc(closest.getArc());
                    } else {
                        slotMockDrawer.setType(SlotCreationPane.getDefaultType());
                        slotMockDrawer.setMount(SlotCreationPane.getDefaultMount());
                        slotMockDrawer.setSize(SlotCreationPane.getDefaultSize());
                        slotMockDrawer.setAngle(SlotCreationPane.getDefaultAngle());
                        slotMockDrawer.setArc(SlotCreationPane.getDefaultArc());
                    }
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

    private void setSlotPaintSize(WorldPoint point, double value) {
        if (point instanceof WeaponSlotPoint checked) {
            checked.setPaintSizeMultiplier(value);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected Class<WeaponSlotPoint> getTypeReference() {
        return WeaponSlotPoint.class;
    }

    @SuppressWarnings("WeakerAccess")
    protected BusEventListener createSelectionListener() {
        return new SharedSelectionListener();
    }

    @Override
    public List<WeaponSlotPoint> getEligibleForSelection() {
        List<WeaponSlotPoint> eligibleForSelection = this.getSlotPoints();
        List<WeaponSlotPoint> result;
        EditorInstrument mode = StaticController.getEditorMode();
        switch (mode) {
            case BUILT_IN_WEAPONS ->
                    result = eligibleForSelection.stream().filter(WeaponSlotPoint::isBuiltIn).toList();
            case DECORATIVES ->
                    result = eligibleForSelection.stream().filter(WeaponSlotPoint::isDecorative).toList();
            case VARIANT_WEAPONS ->
                    result = eligibleForSelection.stream().filter(WeaponSlotPoint::isFittable).toList();
            case VARIANT_MODULES ->
                    result = eligibleForSelection.stream().filter(WeaponSlotPoint::isModule).toList();
            default -> result = eligibleForSelection;
        }
        return result;
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        var visibility = this.getVisibilityMode();
        boolean isRightMode = visibility == ALWAYS_SHOWN || visibility == SHOWN_WHEN_EDITED;
        boolean visibleForRelatedMode = isVisibleForRelatedMode() && isRightMode;
        if (checkVisibility()) {
            this.paintPainterContent(g, worldToScreen, w, h);

            this.handleSelectionHighlight();
            this.paintDelegates(g, worldToScreen, w, h);
        } else if (visibleForRelatedMode) {
            this.handleSelectionHighlight();
            List<WeaponSlotPoint> activePoints = this.getEligibleForSelection();
            this.paintSlots(g, worldToScreen, w, h, activePoints);
        }
    }

    private boolean isVisibleForRelatedMode() {
        return isParentLayerActive() && SharedSelectionListener.isRelatedEditorModeActive();
    }

    private boolean isSlotSelectionEnabled() {
        return this.isInteractionEnabled() || isVisibleForRelatedMode();
    }

    private class SharedSelectionListener implements BusEventListener {
        @Override
        public void handleEvent(BusEvent event) {
            if (event instanceof PointSelectQueued checked && WeaponSlotPainter.this.isPointEligible(checked.point())) {
                if (!isSlotSelectionEnabled()) return;
                WeaponSlotPainter.this.handlePointSelectionEvent((BaseWorldPoint) checked.point());
            }
        }

        private static boolean isRelatedEditorModeActive() {
            EditorInstrument editorMode = StaticController.getEditorMode();
            switch (editorMode) {
                case BUILT_IN_WEAPONS, DECORATIVES, VARIANT_WEAPONS, VARIANT_MODULES -> {
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }
    }

}
