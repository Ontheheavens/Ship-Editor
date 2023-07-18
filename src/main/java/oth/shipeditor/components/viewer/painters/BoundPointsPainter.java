package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.points.BoundCreationQueued;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.PrimaryShipViewer;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.05.2023
 */
@SuppressWarnings("OverlyComplexClass")
@Log4j2
public final class BoundPointsPainter extends AbstractPointPainter {

    @Getter
    private final List<BoundPoint> boundPoints;

    private boolean appendBoundHotkeyPressed;
    private boolean insertBoundHotkeyPressed;

    private final PrimaryShipViewer viewerPanel;

    private final LayerPainter parentLayer;

    private final int appendBoundHotkey = KeyEvent.VK_Z;
    private final int insertBoundHotkey = KeyEvent.VK_X;

    public BoundPointsPainter(PrimaryShipViewer viewer, LayerPainter associatedLayer) {
        this.viewerPanel = viewer;
        this.parentLayer = associatedLayer;
        this.boundPoints = new ArrayList<>();
        this.initHotkeys();
        this.initModeListener();
        this.initCreationListener();
        this.setInteractionEnabled(InstrumentTabsPane.getCurrentMode() == InstrumentMode.BOUNDS);
    }

    @Override
    public boolean isInteractionEnabled() {
        return super.isInteractionEnabled() && this.parentLayer.isLayerActive();
    }

    @Override
    public boolean isMirrorable() {
        return true;
    }

    @Override
    public BaseWorldPoint getMirroredCounterpart(WorldPoint point) {
        List<BoundPoint> bounds = this.getBoundPoints();
        Point2D pointPosition = point.getPosition();
        Point2D counterpartPosition = this.createCounterpartPosition(pointPosition);
        double threshold = ControlPredicates.getMirrorPointLinkageTolerance();
        BoundPoint closestBound = null;
        double closestDistance = Double.MAX_VALUE;
        for (BoundPoint bound : bounds) {
            Point2D position = bound.getPosition();
            if (position.equals(counterpartPosition)) {
                closestBound = bound;
                return closestBound;
            }
            double distance = counterpartPosition.distance(position);
            if (distance < closestDistance) {
                closestBound = bound;
                closestDistance = distance;
            }
        }
        if (closestDistance <= threshold) {
            return closestBound; // Found the mirrored counterpart within the threshold.
        } else {
            return null; // Mirrored counterpart not found.
        }
    }

    @Override
    protected Point2D createCounterpartPosition(Point2D toMirror) {
        ShipCenterPoint shipCenter = parentLayer.getShipCenter();
        Point2D centerPosition = shipCenter.getPosition();
        double counterpartX = 2 * centerPosition.getX() - toMirror.getX();
        double counterpartY = toMirror.getY(); // Y-coordinate remains the same.
        return new Point2D.Double(counterpartX, counterpartY);
    }

    @Override
    protected BoundPoint getTypeReference() {
        return new BoundPoint(new Point2D.Double());
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

    @Override
    protected boolean isParentLayerActive() {
        return this.parentLayer.isLayerActive();
    }

    private void initCreationListener() {
        EventBus.subscribe(event -> {
            if (event instanceof BoundCreationQueued checked) {
                if (!isInteractionEnabled()) return;
                if (!hasPointAtCoords(checked.position())) {
                    createBound(checked);
                }
            }
        });
    }

    private void createBound(BoundCreationQueued event) {
        Point2D position = event.position();
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        if (insertBoundHotkeyPressed) {
            if (boundPoints.size() >= 2) {
                BoundPoint preceding = getInsertBefore(position);
                BoundPoint wrapped = new BoundPoint(position, this.parentLayer);
                BoundPoint wrappedCounterpart = null;
                BoundPoint precedingCounterpart = null;
                if (mirrorMode) {
                    if (getMirroredCounterpart(wrapped) == null) {
                        Point2D counterpartPosition = createCounterpartPosition(position);
                        precedingCounterpart = getInsertBefore(counterpartPosition);
                        wrappedCounterpart = new BoundPoint(counterpartPosition, this.parentLayer);
                    }
                }
                EditDispatch.postPointInserted(this, wrapped, boundPoints.indexOf(preceding));
                if (wrappedCounterpart != null) {
                    EditDispatch.postPointInserted(this, wrappedCounterpart,
                            boundPoints.indexOf(precedingCounterpart));
                }
            }
        } else if (appendBoundHotkeyPressed) {
            BoundPoint wrapped = new BoundPoint(position, this.parentLayer);
            EditDispatch.postPointAdded(this, wrapped);
            if (mirrorMode) {
                if (getMirroredCounterpart(wrapped) == null) {
                    Point2D counterpartPosition = createCounterpartPosition(position);
                    BoundPoint wrappedCounterpart = new BoundPoint(counterpartPosition, this.parentLayer);
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
    protected boolean checkVisibility() {
        PainterVisibility visibilityMode = getVisibilityMode();
        boolean parentCheck = super.checkVisibility();
        if (visibilityMode == PainterVisibility.SHOWN_WHEN_SELECTED && !parentLayer.isLayerActive()) return false;
        return parentCheck;
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!checkVisibility()) return;
        Stroke origStroke = g.getStroke();
        Paint origPaint = g.getPaint();

        int rule = AlphaComposite.SRC_OVER;
        float alpha = this.getPaintOpacity();
        Composite old = g.getComposite();
        Composite opacity = AlphaComposite.getInstance(rule, alpha) ;
        g.setComposite(opacity);

        List<BoundPoint> bPoints = this.boundPoints;
        if (bPoints.isEmpty()) {
            this.paintIfBoundsEmpty(g, worldToScreen);
            g.setStroke(origStroke);
            g.setPaint(origPaint);
            return;
        }
        BoundPoint boundPoint = bPoints.get(bPoints.size() - 1);
        Point2D prev = worldToScreen.transform(boundPoint.getPosition(), null);
        for (BoundPoint p : bPoints) {
            Point2D curr = worldToScreen.transform(p.getPosition(), null);
            Utility.drawBorderedLine(g, prev, curr, Color.LIGHT_GRAY);
            prev = curr;
        }
        // Set the color to white for visual convenience.
        BoundPoint anotherBoundPoint = bPoints.get(0);
        Point2D first = worldToScreen.transform(anotherBoundPoint.getPosition(), null);
        Utility.drawBorderedLine(g, prev, first, Color.GREEN);
        boolean hotkeyPressed = appendBoundHotkeyPressed || insertBoundHotkeyPressed;
        if (isInteractionEnabled() && hotkeyPressed) {
            this.paintCreationGuidelines(g, worldToScreen, prev, first);
        }
        drawSelectionHighlight(g, worldToScreen);
        g.setStroke(origStroke);
        g.setPaint(origPaint);
        super.paintDelegates(g, worldToScreen, w, h);
        g.setComposite(old);
    }

    private void drawSelectionHighlight(Graphics2D g, AffineTransform worldToScreen) {
        WorldPoint selection = this.getSelected();
        if (selection != null && isInteractionEnabled()) {
            float radius = 1.0f;
            BoundPointsPainter.paintPointDot(g, worldToScreen, selection.getPosition(), radius);
            WorldPoint counterpart = this.getMirroredCounterpart(selection);
            if (counterpart != null && ControlPredicates.isMirrorModeEnabled()) {
                BoundPointsPainter.paintPointDot(g, worldToScreen, counterpart.getPosition(), radius);
            }
        }
    }

    private void paintIfBoundsEmpty(Graphics2D g, AffineTransform worldToScreen) {
        ViewerControl viewerControl = viewerPanel.getControls();
        Point2D cursor = viewerControl.getAdjustedCursor();
        AffineTransform screenToWorld = viewerPanel.getScreenToWorld();
        Point2D adjustedWorldCursor = Utility.correctAdjustedCursor(cursor, screenToWorld);
        Point2D worldCounterpart = this.createCounterpartPosition(adjustedWorldCursor);
        boolean hotkeyPressed = appendBoundHotkeyPressed || insertBoundHotkeyPressed;
        if (!isInteractionEnabled() || !hotkeyPressed) return;
        BoundPointsPainter.paintPointDot(g, worldToScreen, adjustedWorldCursor, 1.0f);
        if (ControlPredicates.isMirrorModeEnabled()) {
            Point2D adjustedScreenCursor = worldToScreen.transform(adjustedWorldCursor, null);
            Point2D adjustedScreenCounterpart = worldToScreen.transform(worldCounterpart, null);
            BoundPointsPainter.paintPointDot(g, worldToScreen, worldCounterpart, 1.0f);
            Utility.drawBorderedLine(g, adjustedScreenCursor, adjustedScreenCounterpart, Color.WHITE);
        }
    }

    private void paintCreationGuidelines(Graphics2D g, AffineTransform worldToScreen,
                                         Point2D prev, Point2D first) {
        ViewerControl viewerControl = viewerPanel.getControls();
        Point2D cursor = viewerControl.getAdjustedCursor();
        AffineTransform screenToWorld = viewerPanel.getScreenToWorld();
        Point2D adjustedWorldCursor = Utility.correctAdjustedCursor(cursor, screenToWorld);
        Point2D adjustedScreenCursor = worldToScreen.transform(adjustedWorldCursor, null);
        Point2D worldCounterpart = this.createCounterpartPosition(adjustedWorldCursor);
        Point2D adjustedScreenCounterpart = worldToScreen.transform(worldCounterpart, null);
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        if (appendBoundHotkeyPressed) {
            if (mirrorMode) {
                Utility.drawBorderedLine(g, prev, adjustedScreenCursor, Color.WHITE);
                Utility.drawBorderedLine(g, adjustedScreenCursor, adjustedScreenCounterpart, Color.WHITE);
                Utility.drawBorderedLine(g, adjustedScreenCounterpart, first, Color.WHITE);
            } else {
                BoundPointsPainter.drawGuidelines(g, prev, first, adjustedScreenCursor);
            }
        } else if (insertBoundHotkeyPressed) {
            this.handleInsertionGuides(g, worldToScreen,
                    adjustedWorldCursor, worldCounterpart);
        }
        // Also paint dots where the points would be placed.
        BoundPointsPainter.paintPointDot(g, worldToScreen, adjustedWorldCursor, 1.0f);
        if (mirrorMode) {
            BoundPointsPainter.paintPointDot(g, worldToScreen, worldCounterpart, 1.0f);
        }
    }

    private static void paintPointDot(Graphics2D g, AffineTransform worldToScreen,
                                      Point2D point, float radiusMult) {
        Color originalColor = g.getColor();
        g.setColor(Color.WHITE);
        Shape worldDot = Utility.createCircle(point, 0.35f * radiusMult);
        Shape screenDot = worldToScreen.createTransformedShape(worldDot);
        Point2D screenPoint = worldToScreen.transform(point, null);
        RectangularShape screenOuterDot = Utility.createCircle(screenPoint, 12.0f * radiusMult);
        int x = (int) screenOuterDot.getX();
        int y = (int) screenOuterDot.getY();
        int width = (int) screenOuterDot.getWidth();
        int height = (int) screenOuterDot.getHeight();
        g.fill(screenDot);
        g.drawOval(x, y, width, height);
        g.setColor(originalColor);
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
                Utility.drawBorderedLine(g, subsequent, transformed, Color.WHITE);
                Utility.drawBorderedLine(g, transformed, transformedCounterpart, Color.WHITE);
                Utility.drawBorderedLine(g, transformedCounterpart, preceding, Color.WHITE);
            } else {
                BoundPointsPainter.drawGuidelines(g, preceding, subsequent, transformed);
                BoundPointsPainter.drawGuidelines(g, precedingTC, subsequentTC, transformedCounterpart);
            }
        } else {
            BoundPointsPainter.drawGuidelines(g, preceding, subsequent, transformed);
        }
    }

    private static void drawGuidelines(Graphics2D g, Point2D preceding, Point2D subsequent, Point2D cursor) {
        Utility.drawBorderedLine(g, preceding, cursor, Color.WHITE);
        Utility.drawBorderedLine(g, subsequent, cursor, Color.WHITE);
    }

}
