package oth.shipeditor.components.viewer.painters.points;

import de.javagl.viewer.Painter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.points.BoundCreationQueued;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.BoundPointsSorted;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.ApplicationDefaults;
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

    private static final Color BOUND_LINE = ApplicationDefaults.BOUND_LINE_COLOR;

    @Setter
    private List<BoundPoint> boundPoints;

    private boolean appendBoundHotkeyPressed;
    private boolean insertBoundHotkeyPressed;

    private final int appendBoundHotkey = KeyEvent.VK_Z;
    private final int insertBoundHotkey = KeyEvent.VK_X;

    public BoundPointsPainter(LayerPainter parent) {
        super(parent);
        this.boundPoints = new ArrayList<>();
        this.initHotkeys();
        this.initModeListener();
        this.initPointListening();
        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.BOUNDS);
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
            throw new IllegalArgumentException("Attempted to add incompatible point to BoundPointsPainter!");
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof BoundPoint checked) {
            boundPoints.remove(checked);
        } else {
            throw new IllegalArgumentException("Attempted to remove incompatible point from BoundPointsPainter!");
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof BoundPoint checked) {
            return boundPoints.indexOf(checked);
        } else {
            throw new IllegalArgumentException("Attempted to access incompatible point in BoundPointsPainter!");
        }
    }

    private void initHotkeys() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            int keyCode = ke.getKeyCode();
            // Remember, single equals is assignments, while double is boolean evaluation.
            // First we evaluate whether the passed keycode is one of our hotkeys, then assign the result to field.
            boolean isAppendHotkey = (keyCode == appendBoundHotkey);
            boolean isInsertHotkey = (keyCode == insertBoundHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isAppendHotkey || isInsertHotkey) {
                        setHotkeyState(isAppendHotkey, true);
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isAppendHotkey || isInsertHotkey) {
                        setHotkeyState(isAppendHotkey, false);
                    }
                    break;
            }
            Events.repaintView();
            return false;
        });
    }

    @SuppressWarnings("DuplicatedCode")
    private void initModeListener() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                setInteractionEnabled(checked.newMode() == InstrumentMode.BOUNDS);
            }
        });
    }

    private void initPointListening() {
        EventBus.subscribe(event -> {
            if (event instanceof BoundCreationQueued checked) {
                if (!isInteractionEnabled()) return;
                if (!hasPointAtCoords(checked.position())) {
                    createBound(checked);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof BoundPointsSorted checked) {
                if (!isInteractionEnabled()) return;
                EditDispatch.postBoundsRearranged(this, this.boundPoints, checked.rearranged());
            }
        });
    }

    private void createBound(BoundCreationQueued event) {
        LayerPainter parentLayer = this.getParentLayer();
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

    @SuppressWarnings("unused")
    public void insertPoint(BoundPoint toInsert, BoundPoint preceding) {
        int precedingIndex = boundPoints.indexOf(preceding);
        this.insertPoint(toInsert, precedingIndex);
    }

    public void insertPoint(BoundPoint toInsert, int precedingIndex) {
        boundPoints.add(precedingIndex, toInsert);
        EventBus.publish(new BoundInsertedConfirmed(toInsert, precedingIndex));
        List<Painter> painters = getDelegates();
        painters.add(toInsert.getPainter());
        log.info("Bound inserted to painter: {}", toInsert);
    }

    private void setHotkeyState(boolean isAppendHotkey, boolean state) {
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
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!checkVisibility()) return;

        float alpha = this.getPaintOpacity();
        Composite old = Utility.setAlphaComposite(g, alpha);

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
        this.handleSelectionHighlight();
        this.paintDelegates(g, worldToScreen, w, h);
        g.setComposite(old);
    }

    @Override
    void paintDelegates(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        super.paintDelegates(g, worldToScreen, w, h);
        for (BoundPoint bound : boundPoints) {
            bound.setPaintSizeMultiplier(1);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    private void drawBoundLine(Graphics2D g, Point2D start, Point2D finish, Color color) {
        DrawUtilities.drawScreenLine(g, start, finish, Color.BLACK, 5.0f);
        DrawUtilities.drawScreenLine(g, start, finish, color, 3.0f);
    }

    private void handleSelectionHighlight() {
        WorldPoint selection = this.getSelected();
        if (selection != null && isInteractionEnabled()) {
            BoundPointsPainter.enlargeBound(selection);
            WorldPoint counterpart = this.getMirroredCounterpart(selection);
            if (counterpart != null && ControlPredicates.isMirrorModeEnabled()) {
                BoundPointsPainter.enlargeBound(counterpart);
            }
        }
    }

    private static void enlargeBound(WorldPoint bound) {
        if (bound instanceof BoundPoint checked) {
            checked.setPaintSizeMultiplier(1.5);
        }
    }

    private void paintIfBoundsEmpty(Graphics2D g, AffineTransform worldToScreen) {
        Point2D cursor = StaticController.getAdjustedCursor();
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D adjustedWorldCursor = Utility.correctAdjustedCursor(cursor, screenToWorld);
        Point2D worldCounterpart = this.createCounterpartPosition(adjustedWorldCursor);
        boolean hotkeyPressed = appendBoundHotkeyPressed || insertBoundHotkeyPressed;
        if (!isInteractionEnabled() || !hotkeyPressed) return;
        if (ControlPredicates.isMirrorModeEnabled()) {
            Point2D adjustedScreenCursor = worldToScreen.transform(adjustedWorldCursor, null);
            Point2D adjustedScreenCounterpart = worldToScreen.transform(worldCounterpart, null);
            this.drawBoundLine(g, adjustedScreenCursor, adjustedScreenCounterpart, BOUND_LINE);
            BoundPointsPainter.paintProspectiveBound(g, worldToScreen, worldCounterpart);
        }
        BoundPointsPainter.paintProspectiveBound(g, worldToScreen, adjustedWorldCursor);
    }

    private void paintCreationGuidelines(Graphics2D g, AffineTransform worldToScreen,
                                         Point2D prev, Point2D first) {
        Point2D cursor =  StaticController.getAdjustedCursor();
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D adjustedWorldCursor = Utility.correctAdjustedCursor(cursor, screenToWorld);
        Point2D adjustedScreenCursor = worldToScreen.transform(adjustedWorldCursor, null);
        Point2D worldCounterpart = this.createCounterpartPosition(adjustedWorldCursor);
        Point2D adjustedScreenCounterpart = worldToScreen.transform(worldCounterpart, null);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        if (appendBoundHotkeyPressed) {
            if (mirrorMode) {
                this.drawBoundLine(g, prev, adjustedScreenCursor, BOUND_LINE);
                this.drawBoundLine(g, adjustedScreenCursor, adjustedScreenCounterpart, BOUND_LINE);
                this.drawBoundLine(g, adjustedScreenCounterpart, first, BOUND_LINE);
            } else {
                this.drawGuidelines(g, prev, first, adjustedScreenCursor);
            }
        } else if (insertBoundHotkeyPressed) {
            this.handleInsertionGuides(g, worldToScreen,
                    adjustedWorldCursor, worldCounterpart);
        }
        // Also paint dots where the points would be placed.
        BoundPointsPainter.paintProspectiveBound(g, worldToScreen, adjustedWorldCursor);
        if (mirrorMode) {
            BoundPointsPainter.paintProspectiveBound(g, worldToScreen, worldCounterpart);
        }
    }

    private static void paintProspectiveBound(Graphics2D g, AffineTransform worldToScreen, Point2D position) {
        Shape hexagon = BoundPoint.getShapeForPoint(worldToScreen, position, 1);
        DrawUtilities.outlineShape(g, hexagon, Color.BLACK, 3);
        DrawUtilities.fillShape(g, hexagon, Color.WHITE);
    }

    @SuppressWarnings("DuplicatedCode")
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
