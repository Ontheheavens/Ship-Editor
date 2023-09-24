package oth.shipeditor.components.viewer.painters.points;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.layers.PainterVisibilityChanged;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.control.PointSelectionMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
@Log4j2
public abstract class AbstractPointPainter implements Painter {

    @Getter @Setter
    private WorldPoint selected;

    @Getter @Setter
    private PainterVisibility visibilityMode;

    @Setter
    private boolean interactionEnabled;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    @Getter
    private final AffineTransform delegateWorldToScreen;

    @Getter
    private float paintOpacity = 1.0f;

    @Getter
    private final List<BusEventListener> listeners;

    protected AbstractPointPainter() {
        this.delegateWorldToScreen = new AffineTransform();
        this.listeners = new ArrayList<>();
        this.initPointListeners();
        this.visibilityMode = PainterVisibility.SHOWN_WHEN_EDITED;
        this.setPaintOpacity(1.0f);
    }

    public boolean isInteractionEnabled() {
        return interactionEnabled && isParentLayerActive();
    }

    protected void setPaintOpacity(float opacity) {
        if (opacity < 0.0f) {
            this.paintOpacity = 0.0f;
        } else this.paintOpacity = Math.min(opacity, 1.0f);
    }

    protected abstract boolean isMirrorable();

    protected void cleanupListeners() {
        listeners.forEach(EventBus::unsubscribe);
    }

    public void cleanupPointPainter() {
        Iterable<BaseWorldPoint> points = new ArrayList<>(this.getPointsIndex());
        for (BaseWorldPoint point : points) {
            point.cleanupForRemoval();
            this.removePoint(point);
        }
        this.cleanupListeners();
    }

    @SuppressWarnings("WeakerAccess")
    protected BusEventListener createSelectionListener() {
        return new SimplePointSelectionListener();
    }

    @SuppressWarnings("OverlyComplexMethod")
    private void initPointListeners() {
        BusEventListener pointRemovalListener = event -> {
            if (event instanceof PointRemoveQueued checked && this.isInteractionEnabled()) {
                this.handlePointRemovalEvent(checked.point(), checked.fromList());
            }
        };
        listeners.add(pointRemovalListener);
        EventBus.subscribe(pointRemovalListener);

        BusEventListener pointSelectionListener = createSelectionListener();
        listeners.add(pointSelectionListener);
        EventBus.subscribe(pointSelectionListener);

        BusEventListener pointDragListener = event -> {
            if (event instanceof PointDragQueued checked) {
                if (!this.isInteractionEnabled()) return;
                if (getSelected() == null) return;
                AffineTransform screenToWorld = checked.screenToWorld();
                Point2D translated = screenToWorld.transform(checked.target(), null);
                double x = translated.getX();
                double y = translated.getY();
                if (ControlPredicates.isCursorSnappingEnabled()) {
                    x = Math.round(x * 2) / 2.0;
                    y = Math.round(y * 2) / 2.0;
                }
                Point2D changedPosition = new Point2D.Double(x, y);

                WorldPoint counterpart = null;
                Point2D counterpartNewPosition = null;
                boolean mirroringEnabled = ControlPredicates.isMirrorModeEnabled();
                if (isMirrorable() && mirroringEnabled) {
                    counterpart = getMirroredCounterpart(getSelected());
                    if (counterpart != null) {
                        counterpartNewPosition = createCounterpartPosition(changedPosition);
                    }
                }
                EditDispatch.postPointDragged(getSelected(), changedPosition);
                if (counterpartNewPosition != null) {
                    EditDispatch.postPointDragged(counterpart, counterpartNewPosition);
                }
            }
        };
        listeners.add(pointDragListener);
        EventBus.subscribe(pointDragListener);
        BusEventListener painterOpacityListener = event -> {
            if (event instanceof PainterOpacityChangeQueued checked) {
                Class<? extends AbstractPointPainter> painterClass = checked.painterClass();
                if (!painterClass.isInstance(this)) return;
                if (!isParentLayerActive()) return;
                this.setPaintOpacity(checked.change());
                EventBus.publish(new ViewerRepaintQueued());
            }
        };
        listeners.add(painterOpacityListener);
        EventBus.subscribe(painterOpacityListener);
        BusEventListener painterVisibilityListener = event -> {
            if (event instanceof PainterVisibilityChanged checked) {
                Class<? extends AbstractPointPainter> painterClass = checked.painterClass();
                if (!painterClass.isInstance(this) || !isParentLayerActive()) return;
                this.setVisibilityMode(checked.changed());
                EventBus.publish(new ViewerRepaintQueued());
            }
        };
        listeners.add(painterVisibilityListener);
        EventBus.subscribe(painterVisibilityListener);
    }

    private void handlePointRemovalEvent(BaseWorldPoint point, boolean removalViaListPanel) {
        Class<? extends BaseWorldPoint> typeReference = getTypeReference();
        if (typeReference.isInstance(point) && removalViaListPanel) {
            this.commencePointRemoval(point);
        } else if (selected != null && !removalViaListPanel) {
            this.commencePointRemoval((BaseWorldPoint) selected);
        }
    }

    private void commencePointRemoval(BaseWorldPoint point) {
        List<? extends BaseWorldPoint> pointsIndex = getPointsIndex();
        if (!pointsIndex.contains(point)) {
            throw new IllegalArgumentException("Point passed for removal is not present in the point painter!");
        }
        boolean mirroringEnabled = ControlPredicates.isMirrorModeEnabled();
        WorldPoint counterpart = null;
        if (isMirrorable() && mirroringEnabled) {
            counterpart = getMirroredCounterpart(point);
        }
        EditDispatch.postPointRemoved(this, point);
        if (counterpart != null) {
            EditDispatch.postPointRemoved(this, (BaseWorldPoint) counterpart);
        }
    }

    protected abstract boolean isParentLayerActive();

    Point2D createCounterpartPosition(Point2D toMirror) {
        throw new UnsupportedOperationException("Point mirroring supported only for specific point painters!");
    }

    @SuppressWarnings("WeakerAccess")
    protected void handlePointSelectionEvent(BaseWorldPoint point) {
        if (point != null) {
            List<? extends BaseWorldPoint> pointsIndex = getPointsIndex();
            if (!pointsIndex.contains(point)) return;
            if (this.selected != null) {
                this.selected.setPointSelected(false);
            }
            this.selected = point;
            this.selected.setPointSelected(true);
            EventBus.publish(new PointSelectedConfirmed(this.selected));
            EventBus.publish(new ViewerRepaintQueued());
        } else {
            selectPointConditionally();
        }
    }

    protected void selectPointConditionally() {
        PointSelectionMode current = ControlPredicates.getSelectionMode();
        if (current == PointSelectionMode.STRICT) {
            this.selectPointStrictly();
            return;
        }
        this.selectPointClosest();
    }

    @SuppressWarnings("WeakerAccess")
    public BaseWorldPoint findClosestPoint(Point2D target) {
        BaseWorldPoint closestPoint = null;
        double closestDistance = Double.MAX_VALUE;

        for (BaseWorldPoint point : this.getEligibleForSelection()) {
            Point2D position = point.getPosition();
            double distance = target.distance(position);

            if (distance < closestDistance) {
                closestPoint = point;
                closestDistance = distance;
            }
        }

        return closestPoint;
    }

    @SuppressWarnings("WeakerAccess")
    protected List<? extends BaseWorldPoint> getEligibleForSelection() {
        return this.getPointsIndex();
    }

    protected void selectPointClosest() {
        Point2D cursor = StaticController.getCorrectedCursor();
        BaseWorldPoint toSelect = findClosestPoint(cursor);

        WorldPoint selectedPoint = this.getSelected();
        if (selectedPoint != null) {
            selectedPoint.setPointSelected(false);
        }
        this.setSelected(toSelect);
        if (toSelect != null) {
            toSelect.setPointSelected(true);
        }
        EventBus.publish(new PointSelectedConfirmed(toSelect));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private void selectPointStrictly() {
        if (!this.isMousedOverPoint()) return;
        if (this.selected != null) {
            this.selected.setPointSelected(false);
        }
        this.selected = this.getMousedOver();
        WorldPoint point = this.getSelected();
        point.setPointSelected(true);
        EventBus.publish(new PointSelectedConfirmed(this.selected));
        EventBus.publish(new ViewerRepaintQueued());
    }

    public abstract List<? extends BaseWorldPoint> getPointsIndex();

    protected abstract void addPointToIndex(BaseWorldPoint point);

    protected abstract void removePointFromIndex(BaseWorldPoint point);

    public abstract int getIndexOfPoint(BaseWorldPoint point);

    protected abstract WorldPoint getMirroredCounterpart(WorldPoint inputPoint);

    protected abstract Class<? extends BaseWorldPoint> getTypeReference();

    @SuppressWarnings("WeakerAccess")
    protected boolean isPointEligible(WorldPoint point) {
        if (point != null) {
            Class<? extends BaseWorldPoint> typeReferenceClass = getTypeReference();
            return typeReferenceClass.isAssignableFrom(point.getClass());
        }
        return true;
    }

    private boolean isMousedOverPoint() {
        return this.getMousedOver() != null;
    }

    private BaseWorldPoint getMousedOver() {
        BaseWorldPoint mousedOver = null;
        for (BaseWorldPoint point : this.getPointsIndex()) {
            if (point.isCursorInBounds()) {
                mousedOver = point;
            }
        }
        return mousedOver;
    }

    public void addPoint(BaseWorldPoint point) {
        this.addPointToIndex(point);
        EventBus.publish(new PointAddConfirmed(point));
    }

    public void removePoint(BaseWorldPoint point) {
        this.removePointFromIndex(point);
        EventBus.publish(new PointRemovedConfirmed(point));
        if (this.selected == point) {
            this.setSelected(null);
        }
        point.setPointSelected(false);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasPointAtCoords(Point2D point2D) {
        boolean pointDoesExist = false;
        for (WorldPoint point : this.getPointsIndex()) {
            Point2D coords = point.getPosition();
            if (point2D.equals(coords)) {
                pointDoesExist = true;
                break;
            }
        }
        return pointDoesExist;
    }

    protected void paintDelegates(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        List<? extends BaseWorldPoint> pointsIndex = this.getPointsIndex();
        pointsIndex.forEach(painter -> paintDelegate(g, worldToScreen, w, h, painter));
    }

    @SuppressWarnings("WeakerAccess")
    protected void paintDelegate(Graphics2D g, AffineTransform worldToScreen, double w, double h, Painter painter) {
        if (painter != null) {
            AffineTransform transform = this.delegateWorldToScreen;
            transform.setTransform(worldToScreen);
            painter.paint(g, transform, w, h);
        }
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!checkVisibility()) return;
        float alpha = this.getPaintOpacity();
        Composite old = Utility.setAlphaComposite(g, alpha);

        paintDelegates(g, worldToScreen, w, h);

        g.setComposite(old);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean checkVisibility() {
        PainterVisibility visibility = getVisibilityMode();
        if (visibility == PainterVisibility.ALWAYS_HIDDEN) return false;
        if (visibility == PainterVisibility.SHOWN_WHEN_EDITED && this.isInteractionEnabled()) return true;
        if (visibility == PainterVisibility.SHOWN_WHEN_SELECTED && this.isParentLayerActive()) return true;
        return visibility == PainterVisibility.ALWAYS_SHOWN;
    }

    @Override
    public String toString() {
        Class<? extends AbstractPointPainter> identity = this.getClass();
        return identity.getSimpleName() + " @" + this.hashCode();
    }

    /**
     * @throws IllegalArgumentException as a fail-fast precaution when illegal point type is detected.
     */
    @SuppressWarnings("WeakerAccess")
    protected void throwIllegalPoint() {
        Class<? extends AbstractPointPainter> identity = this.getClass();
        throw new IllegalArgumentException("Illegal point type in " + identity.getSimpleName());
    }

    private class SimplePointSelectionListener implements BusEventListener {
        @Override
        public void handleEvent(BusEvent event) {
            if (event instanceof PointSelectQueued checked && AbstractPointPainter.this.isPointEligible(checked.point())) {
                if (!AbstractPointPainter.this.isInteractionEnabled()) return;
                AbstractPointPainter.this.handlePointSelectionEvent((BaseWorldPoint) checked.point());
            }
        }
    }

}