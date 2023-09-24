package oth.shipeditor.components.viewer.painters.points.ship;

import com.jhlabs.image.HSBAdjustFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.engine.EngineDataOverride;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.points.AngledPointPainter;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.08.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
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
    protected void initInteractionListeners() {
        super.initInteractionListeners();
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

            double minimumValue = 0.5d;
            if (fullWidth < minimumValue) {
                fullWidth = minimumValue;
            }
            if (length < minimumValue) {
                length = minimumValue;
            }

            this.changeEngineSizeWithMirrorCheck(checked, new Size2D(fullWidth, length));
        } else if (selected !=null) {
            throwIllegalPoint();
        }
    }

    public void changeEngineSizeWithMirrorCheck(EnginePoint point, Size2D size) {
        point.changeSize(size);
        actOnCounterpart(enginePoint -> enginePoint.changeSize(size), point);
    }

    public void changeEngineContrailWithMirrorCheck(EnginePoint point, int contrailSize) {
        point.changeContrailSize(contrailSize);
        actOnCounterpart(enginePoint -> enginePoint.changeContrailSize(contrailSize), point);
    }

    public void changeEngineStyleWithMirrorCheck(EnginePoint point, EngineStyle style) {
        point.changeStyle(style);
        actOnCounterpart(enginePoint -> enginePoint.changeStyle(style), point);
    }

    public void resetSkinSlotOverride() {
        this.enginePoints.forEach(enginePoint -> enginePoint.setSkinOverride(null));
    }

    public void toggleSkinSlotOverride(ShipSkin skin) {
        this.enginePoints.forEach(enginePoint -> this.setEngineOverrideFromSkin(enginePoint, skin));
    }

    private void setEngineOverrideFromSkin(EnginePoint enginePoint, ShipSkin skin) {
        if (skin == null || skin.isBase()) {
            enginePoint.setSkinOverride(null);
            return;
        }
        int slotIndex = this.enginePoints.indexOf(enginePoint);
        Map<Integer, EngineDataOverride> engineSlotChanges = skin.getEngineSlotChanges();
        EngineDataOverride matchingOverride = engineSlotChanges.get(slotIndex);
        enginePoint.setSkinOverride(matchingOverride);
    }

    @Override
    protected EditorInstrument getInstrumentType() {
        return EditorInstrument.ENGINES;
    }

    @Override
    protected void handleCreation(PointCreationQueued event) {
        if (!isCreationHotkeyPressed()) return;

        ShipPainter parentLayer = (ShipPainter) this.getParentLayer();
        Point2D position = event.position();
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();

        EnginePoint created;
        EnginePoint counterpart = null;

        String lowTech = "LOW_TECH";
        if (enginePoints.isEmpty()) {
            created = new EnginePoint(position, parentLayer);

            created.setAngle(180);
            created.setLength(10);
            created.setWidth(2);
            created.setContrailSize(12);
            created.setStyleID(lowTech);

        } else {
            EnginePoint closest = (EnginePoint) findClosestPoint(position);
            if (closest != null) {
                created = new EnginePoint(position, parentLayer, closest);
            } else {
                created = new EnginePoint(position, parentLayer);

                created.setAngle(180);
                created.setLength(10);
                created.setWidth(2);
                created.setContrailSize(12);
                created.setStyleID(lowTech);
            }
        }

        if (mirrorMode) {
            if (getMirroredCounterpart(created) == null) {
                Point2D counterpartPosition = createCounterpartPosition(position);
                counterpart = new EnginePoint(counterpartPosition, parentLayer, created);
                double flipAngle = Utility.flipAngle(counterpart.getAngle());
                counterpart.setAngle(flipAngle);
            }
        }

        EditDispatch.postPointAdded(this, created);
        if (counterpart != null) {
            EditDispatch.postPointAdded(this, counterpart);
        }
    }

    @Override
    public void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        super.paintPainterContent(g, worldToScreen, w, h);

        if (isInteractionEnabled() && isCreationHotkeyPressed()) {
            Point2D finalWorldCursor = StaticController.getFinalWorldCursor();
            Point2D worldCounterpart = this.createCounterpartPosition(finalWorldCursor);
            boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();

            Color flameColor = new Color(255, 125, 25);
            double rawAngle = 180;
            double engineWidth = 2;
            double engineLength = 10;

            if (!enginePoints.isEmpty()) {
                EnginePoint closest = (EnginePoint) findClosestPoint(finalWorldCursor);
                if (closest != null) {
                    EngineStyle style = closest.getStyle();
                    if (style != null) {
                        flameColor = style.getEngineColor();
                    }
                    rawAngle = closest.getAngle();
                    engineWidth = closest.getWidth();
                    engineLength = closest.getLength();
                }
            }

            float[] hue = Color.RGBtoHSB(flameColor.getRed(), flameColor.getGreen(), flameColor.getBlue(), null);
            BufferedImageOp filter = new HSBAdjustFilter(hue[0], hue[1], hue[2]);
            BufferedImage flameColored = filter.filter(EnginePoint.getBaseFlameTexture(), null);

            EnginePoint.drawRectangleStatically(g, worldToScreen, finalWorldCursor,
                    rawAngle, engineWidth, engineLength);
            EnginePoint.drawFlameStatically(g, worldToScreen, finalWorldCursor, rawAngle, engineWidth,
                    engineLength, flameColored);

            if (mirrorMode) {
                double flipAngle = Utility.flipAngle(rawAngle);

                EnginePoint.drawRectangleStatically(g, worldToScreen, worldCounterpart,
                        flipAngle, engineWidth, engineLength);
                EnginePoint.drawFlameStatically(g, worldToScreen, worldCounterpart, flipAngle, engineWidth,
                        engineLength, flameColored);
            }
        }
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
        if (toInsert instanceof EnginePoint checked) {
            enginePoints.add(precedingIndex, checked);
            EventBus.publish(new EngineInsertedConfirmed(checked, precedingIndex));
            log.info("Engine inserted to painter: {}", checked);
        }
        else {
            throwIllegalPoint();
        }

    }

}
