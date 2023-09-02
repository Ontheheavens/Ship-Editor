package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.BoundPointsSorted;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.05.2023
 */
@Log4j2
public final class BoundPointsPainter extends MirrorablePointPainter {

    private static final Color BOUND_LINE = Color.WHITE;

    @Setter
    private List<BoundPoint> boundPoints;

    @Getter
    private static boolean appendBoundHotkeyPressed;

    @Getter
    private static boolean insertBoundHotkeyPressed;

    private final int appendBoundHotkey = KeyEvent.VK_Z;
    private final int insertBoundHotkey = KeyEvent.VK_X;

    private KeyEventDispatcher hotkeyDispatcher;

    public BoundPointsPainter(ShipPainter parent) {
        super(parent);
        this.boundPoints = new ArrayList<>();
        this.initHotkeys();
        this.initPointListening();
    }

    @Override
    protected EditorInstrument getInstrumentType() {
        return EditorInstrument.BOUNDS;
    }

    @Override
    public void cleanupListeners() {
        super.cleanupListeners();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(hotkeyDispatcher);
    }

    @Override
    protected Class<BoundPoint> getTypeReference() {
        return BoundPoint.class;
    }

    @Override
    public List<BoundPoint> getPointsIndex() {
        return boundPoints;
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof BoundPoint checked) {
            boundPoints.add(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof BoundPoint checked) {
            boundPoints.remove(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof BoundPoint checked) {
            return boundPoints.indexOf(checked);
        } else {
            throwIllegalPoint();
            return -1;
        }
    }

    private void initHotkeys() {
        hotkeyDispatcher = ke -> {
            int keyCode = ke.getKeyCode();
            // Remember, single equals is assignments, while double is boolean evaluation.
            // First we evaluate whether the passed keycode is one of our hotkeys, then assign the result to field.
            boolean isAppendHotkey = (keyCode == appendBoundHotkey);
            boolean isInsertHotkey = (keyCode == insertBoundHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isAppendHotkey || isInsertHotkey) {
                        BoundPointsPainter.setHotkeyState(isAppendHotkey, true);
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isAppendHotkey || isInsertHotkey) {
                        BoundPointsPainter.setHotkeyState(isAppendHotkey, false);
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(hotkeyDispatcher);
    }

    private void initPointListening() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener boundsSortingListener = event -> {
            if (event instanceof BoundPointsSorted checked) {
                if (!isInteractionEnabled()) return;
                EditDispatch.postBoundsRearranged(this, this.boundPoints, checked.rearranged());
            }
        };
        listeners.add(boundsSortingListener);
        EventBus.subscribe(boundsSortingListener);
    }

    protected void handleCreation(PointCreationQueued event) {
        ShipPainter parentLayer = (ShipPainter) this.getParentLayer();
        Point2D position = event.position();
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        if (insertBoundHotkeyPressed) {
            if (boundPoints.size() >= 2) {
                BoundPoint preceding = getInsertBefore(position);
                BoundPoint wrapped = new BoundPoint(position, parentLayer);
                BoundPoint wrappedCounterpart = null;
                BoundPoint precedingCounterpart = null;
                if (mirrorMode) {
                    if (getMirroredCounterpart(wrapped) == null) {
                        Point2D counterpartPosition = createCounterpartPosition(position);
                        precedingCounterpart = getInsertBefore(counterpartPosition);
                        wrappedCounterpart = new BoundPoint(counterpartPosition, parentLayer);
                    }
                }
                EditDispatch.postPointInserted(this, wrapped, boundPoints.indexOf(preceding));
                if (wrappedCounterpart != null) {
                    EditDispatch.postPointInserted(this, wrappedCounterpart,
                            boundPoints.indexOf(precedingCounterpart));
                }
            }
        } else if (appendBoundHotkeyPressed) {
            BoundPoint wrapped = new BoundPoint(position, parentLayer);
            EditDispatch.postPointAdded(this, wrapped);
            if (mirrorMode) {
                if (getMirroredCounterpart(wrapped) == null) {
                    Point2D counterpartPosition = createCounterpartPosition(position);
                    BoundPoint wrappedCounterpart = new BoundPoint(counterpartPosition, parentLayer);
                    EditDispatch.postPointInserted(this, wrappedCounterpart, 0);
                }
            }
        }
    }

    private BoundPoint getInsertBefore(Point2D position) {
        List<BoundPoint> twoClosest = findClosestBoundPoints(position);
        return twoClosest.get(1);
    }

    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {
        if (toInsert instanceof BoundPoint checked) {
            boundPoints.add(precedingIndex, checked);
            EventBus.publish(new BoundInsertedConfirmed(checked, precedingIndex));
            log.info("Bound inserted to painter: {}", checked);
        }
        else {
            throwIllegalPoint();
        }
    }

    private static void setHotkeyState(boolean isAppendHotkey, boolean state) {
        if (isAppendHotkey) {
            appendBoundHotkeyPressed = state;
        } else {
            insertBoundHotkeyPressed = state;
        }
    }

    private List<BoundPoint> findClosestBoundPoints(Point2D point) {
        double minDist = Double.MAX_VALUE;
        List<BoundPoint> closestPoints = new ArrayList<>(2);
        List<BoundPoint> bounds = this.boundPoints;
        int numPoints = bounds.size();
        for (int i = 0; i < numPoints; i++) {
            BoundPoint currentPoint = bounds.get(i);
            // Wrap around to the first point if it's the last segment.
            BoundPoint nextPoint = bounds.get((i + 1) % numPoints);
            Line2D segment = new Line2D.Double(currentPoint.getPosition(), nextPoint.getPosition());
            double dist = segment.ptSegDist(point);
            if (dist < minDist) {
                minDist = dist;
                closestPoints.clear();
                closestPoints.add(currentPoint);
                closestPoints.add(nextPoint);
            }
        }
        return closestPoints;
    }

    @Override
    public void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        List<BoundPoint> bPoints = this.boundPoints;
        if (bPoints.isEmpty()) {
            this.paintIfBoundsEmpty(g, worldToScreen);
            return;
        }
        BoundPoint boundPoint = bPoints.get(bPoints.size() - 1);
        Point2D prev = worldToScreen.transform(boundPoint.getPosition(), null);
        for (BoundPoint p : bPoints) {
            Point2D curr = worldToScreen.transform(p.getPosition(), null);
            this.drawBoundLine(g, prev, curr, BOUND_LINE);
            prev = curr;
        }
        BoundPoint anotherBoundPoint = bPoints.get(0);
        Point2D first = worldToScreen.transform(anotherBoundPoint.getPosition(), null);

        this.drawBoundLine(g, prev, first, Color.GREEN);

        boolean hotkeyPressed = appendBoundHotkeyPressed || insertBoundHotkeyPressed;
        if (isInteractionEnabled() && hotkeyPressed) {
            this.paintCreationGuidelines(g, worldToScreen, prev, first);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    private void drawBoundLine(Graphics2D g, Point2D start, Point2D finish, Color color) {
        DrawUtilities.drawScreenLine(g, start, finish, Color.BLACK, 5.0f);
        DrawUtilities.drawScreenLine(g, start, finish, color, 3.0f);
    }

    private void paintIfBoundsEmpty(Graphics2D g, AffineTransform worldToScreen) {
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D finalWorldCursor = screenToWorld.transform(StaticController.getRawCursor(), null);
        if (ControlPredicates.isCursorSnappingEnabled()) {
            Point2D cursor = StaticController.getAdjustedCursor();
            finalWorldCursor = Utility.correctAdjustedCursor(cursor, screenToWorld);
        }
        Point2D worldCounterpart = this.createCounterpartPosition(finalWorldCursor);
        boolean hotkeyPressed = appendBoundHotkeyPressed || insertBoundHotkeyPressed;
        if (!isInteractionEnabled() || !hotkeyPressed) return;
        if (ControlPredicates.isMirrorModeEnabled()) {
            Point2D adjustedScreenCursor = worldToScreen.transform(finalWorldCursor, null);
            Point2D adjustedScreenCounterpart = worldToScreen.transform(worldCounterpart, null);
            this.drawBoundLine(g, adjustedScreenCursor, adjustedScreenCounterpart, BOUND_LINE);
            BoundPointsPainter.paintProspectiveBound(g, worldToScreen, worldCounterpart);
        }
        BoundPointsPainter.paintProspectiveBound(g, worldToScreen, finalWorldCursor);
    }

    private void paintCreationGuidelines(Graphics2D g, AffineTransform worldToScreen,
                                         Point2D prev, Point2D first) {
        Point2D finalWorldCursor = StaticController.getFinalWorldCursor();
        Point2D finalScreenCursor = worldToScreen.transform(finalWorldCursor, null);
        Point2D worldCounterpart = this.createCounterpartPosition(finalWorldCursor);
        Point2D adjustedScreenCounterpart = worldToScreen.transform(worldCounterpart, null);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        if (appendBoundHotkeyPressed) {
            if (mirrorMode) {
                this.drawBoundLine(g, prev, finalScreenCursor, BOUND_LINE);
                this.drawBoundLine(g, finalScreenCursor, adjustedScreenCounterpart, BOUND_LINE);
                this.drawBoundLine(g, adjustedScreenCounterpart, first, BOUND_LINE);
            }
            else {
                this.drawGuidelines(g, prev, first, finalScreenCursor);
            }
        }
        else if (insertBoundHotkeyPressed) {
            this.handleInsertionGuides(g, worldToScreen,
                    finalWorldCursor, worldCounterpart);
        }
        // Also paint dots where the points would be placed.
        BoundPointsPainter.paintProspectiveBound(g, worldToScreen, finalWorldCursor);
        if (mirrorMode) {
            BoundPointsPainter.paintProspectiveBound(g, worldToScreen, worldCounterpart);
        }
    }

    private static void paintProspectiveBound(Graphics2D g, AffineTransform worldToScreen, Point2D position) {
        Shape hexagon = BoundPoint.getShapeForPoint(worldToScreen, position, 1);
        DrawUtilities.outlineShape(g, hexagon, Color.BLACK, 3);
        DrawUtilities.fillShape(g, hexagon, Color.WHITE);
    }

    private void handleInsertionGuides(Graphics2D g, AffineTransform worldToScreen,
                                       Point2D adjustedWorldCursor, Point2D worldCounterpart) {
        List<BoundPoint> closest = this.findClosestBoundPoints(adjustedWorldCursor);
        BoundPoint precedingPoint = closest.get(1);
        Point2D preceding = worldToScreen.transform(precedingPoint.getPosition(), null);
        BoundPoint subsequentPoint = closest.get(0);
        Point2D subsequent = worldToScreen.transform(subsequentPoint.getPosition(), null);
        Point2D transformed = worldToScreen.transform(adjustedWorldCursor, null);

        List<BoundPoint> closestToCounterpart = this.findClosestBoundPoints(worldCounterpart);
        BoundPoint precedingToCounterpart = closestToCounterpart.get(1);
        Point2D precedingTC = worldToScreen.transform(precedingToCounterpart.getPosition(), null);
        BoundPoint subsequentToCounterpart = closestToCounterpart.get(0);
        Point2D subsequentTC = worldToScreen.transform(subsequentToCounterpart.getPosition(), null);
        Point2D transformedCounterpart = worldToScreen.transform(worldCounterpart, null);

        boolean crossingEmerged = preceding.equals(precedingTC) || subsequent.equals(subsequentTC);

        if (ControlPredicates.isMirrorModeEnabled()) {
            if (crossingEmerged) {
                this.drawBoundLine(g, subsequent, transformed, BOUND_LINE);
                this.drawBoundLine(g, transformed, transformedCounterpart, BOUND_LINE);
                this.drawBoundLine(g, transformedCounterpart, preceding, BOUND_LINE);
            } else {
                this.drawGuidelines(g, preceding, subsequent, transformed);
                this.drawGuidelines(g, precedingTC, subsequentTC, transformedCounterpart);
            }
        } else {
            this.drawGuidelines(g, preceding, subsequent, transformed);
        }
    }

    private void drawGuidelines(Graphics2D g, Point2D preceding, Point2D subsequent, Point2D cursor) {
        this.drawBoundLine(g, preceding, cursor, BOUND_LINE);
        this.drawBoundLine(g, subsequent, cursor, BOUND_LINE);
    }

}
