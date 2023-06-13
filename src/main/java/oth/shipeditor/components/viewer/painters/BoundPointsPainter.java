package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.BoundCreationQueued;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.ShipViewerPanel;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.utility.Utility;

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
public final class BoundPointsPainter extends AbstractPointPainter {

    // TODO: implement always-show-points checkbox toggle and separate interaction access check.
    //  Should be unable to interact with bounds unless bound tab is active.

    @Getter
    private final List<BoundPoint> boundPoints;

    private boolean appendBoundHotkeyPressed;
    private boolean insertBoundHotkeyPressed;

    private final ShipViewerPanel viewerPanel;

    private final ShipLayer parentLayer;

    private final int appendBoundHotkey = KeyEvent.VK_Z;
    private final int insertBoundHotkey = KeyEvent.VK_X;

    public BoundPointsPainter(ShipViewerPanel viewer, ShipLayer associatedLayer) {
        this.viewerPanel = viewer;
        this.parentLayer = associatedLayer;
        this.boundPoints = new ArrayList<>();
        this.initHotkeys();
        this.initModeListener();
        this.initCreationListener();
        this.setInteractionEnabled(false);
    }

    @Override
    public boolean isInteractionEnabled() {
        LayerManager layerManager = viewerPanel.getLayerManager();
        boolean layerActive = (layerManager.getActiveLayer() == this.parentLayer);
        return super.isInteractionEnabled() && layerActive;
    }

    @Override
    protected BoundPoint getTypeReference() {
        return new BoundPoint(new Point2D.Double());
    }

    @Override
    protected List<BoundPoint> getPointsIndex() {
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

    private static void queueViewerRepaint() {
        EventBus.publish(new ViewerRepaintQueued());
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
            BoundPointsPainter.queueViewerRepaint();
            return false;
        });
    }

    private void initModeListener() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentModeChanged checked) {
                setInteractionEnabled(checked.newMode() == InstrumentMode.BOUNDS);
            }
        });
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
        if (insertBoundHotkeyPressed) {
            List<BoundPoint> boundPointList = boundPoints;
            if (boundPointList.size() >= 2) {
                List<BoundPoint> twoClosest = findClosestBoundPoints(position);
                int index = getLowestBoundPointIndex(twoClosest);
                if (index >= 0) index += 1;
                if (index > boundPointList.size() - 1) index = 0;
                if (getHighestBoundPointIndex(twoClosest) == boundPointList.size() - 1 &&
                        getLowestBoundPointIndex(twoClosest) == 0) index = 0;
                BoundPoint preceding = boundPointList.get(index);
                BoundPoint wrapped = new BoundPoint(position);
                insertPoint(wrapped, preceding);
                BoundPointsPainter.queueViewerRepaint();
            }
        } else if (appendBoundHotkeyPressed) {
            BoundPoint wrapped = new BoundPoint(position);
            addPoint(wrapped);
            BoundPointsPainter.queueViewerRepaint();
        }
    }

    private void insertPoint(BoundPoint toInsert, BoundPoint preceding) {
        int precedingIndex = boundPoints.indexOf(preceding);
        boundPoints.add(precedingIndex, toInsert);
        EventBus.publish(new BoundInsertedConfirmed(toInsert, precedingIndex));
        List<Painter> painters = getDelegates();
        painters.add(toInsert.getPainter());
        log.info(toInsert);
    }

    private void setHotkeyState(boolean isAppendHotkey, boolean state) {
        if (isAppendHotkey) {
            appendBoundHotkeyPressed = state;
        } else {
            insertBoundHotkeyPressed = state;
        }
    }

    private List<BoundPoint> findClosestBoundPoints(Point2D cursor) {
        List<BoundPoint> pointList = new ArrayList<>(this.boundPoints);
        pointList.add(pointList.get(0)); // Add first point to end of list.
        BoundPoint closestPoint1 = pointList.get(0);
        BoundPoint closestPoint2 = pointList.get(1);
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < pointList.size() - 1; i++) {
            BoundPoint currentPoint = pointList.get(i);
            BoundPoint nextPoint = pointList.get(i+1);
            Line2D segment = new Line2D.Double(currentPoint.getPosition(), nextPoint.getPosition());
            double dist = segment.ptSegDist(cursor);
            if (dist < minDist) {
                closestPoint1 = currentPoint;
                closestPoint2 = nextPoint;
                minDist = dist;
            }
        }
        List<BoundPoint> closestPoints = new ArrayList<>(2);
        closestPoints.add(closestPoint1);
        closestPoints.add(closestPoint2);
        return closestPoints;
    }

    private int getLowestBoundPointIndex(List<BoundPoint> closestPoints) {
        List<BoundPoint> points = boundPoints;
        int index1 = points.indexOf(closestPoints.get(0));
        int index2 = points.indexOf(closestPoints.get(1));
        return Math.min(index1, index2);
    }

    private int getHighestBoundPointIndex(List<BoundPoint> closestPoints) {
        List<BoundPoint> points = this.boundPoints;
        int index1 = points.indexOf(closestPoints.get(0));
        int index2 = points.indexOf(closestPoints.get(1));
        return Math.max(index1, index2);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        List<BoundPoint> bPoints = this.boundPoints;
        if (bPoints.isEmpty()) return;
        Stroke origStroke = g.getStroke();
        Paint origPaint = g.getPaint();
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
        Utility.drawBorderedLine(g, prev, first, Color.DARK_GRAY);
        boolean hotkeyPressed = appendBoundHotkeyPressed || insertBoundHotkeyPressed;
        if (isInteractionEnabled() && hotkeyPressed) {
            this.paintCreationGuidelines(g, worldToScreen, prev, first);
        }
        g.setStroke(origStroke);
        g.setPaint(origPaint);
        super.paintDelegates(g, worldToScreen, w, h);
    }

    private void paintCreationGuidelines(Graphics2D g, AffineTransform worldToScreen,
                                         Point2D prev, Point2D first) {
        ViewerControl viewerControl = viewerPanel.getControls();
        Point2D cursor = viewerControl.getAdjustedCursor();
        AffineTransform screenToWorld = viewerPanel.getScreenToWorld();
        Point2D adjusted = worldToScreen.transform(Utility.correctAdjustedCursor(cursor, screenToWorld), null);
        if (appendBoundHotkeyPressed) {
            Utility.drawBorderedLine(g, prev, adjusted, Color.WHITE);
            Utility.drawBorderedLine(g, adjusted, first, Color.WHITE);
        } else if (insertBoundHotkeyPressed) {
            Point2D transformed = screenToWorld.transform(adjusted, null);
            List<BoundPoint> closest = this.findClosestBoundPoints(transformed);
            BoundPoint precedingPoint = closest.get(1);
            Point2D preceding = worldToScreen.transform(precedingPoint.getPosition(), null);
            BoundPoint subsequentPoint = closest.get(0);
            Point2D subsequent = worldToScreen.transform(subsequentPoint.getPosition(), null);
            Utility.drawBorderedLine(g, preceding, adjusted, Color.WHITE);
            Utility.drawBorderedLine(g, subsequent, adjusted, Color.WHITE);
        }
    }

}
