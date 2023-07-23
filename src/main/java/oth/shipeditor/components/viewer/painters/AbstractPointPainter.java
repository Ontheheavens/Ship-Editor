package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.layers.PainterVisibilityChanged;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.control.PointSelectionMode;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.undo.EditDispatch;
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

    @Getter
    protected final List<Painter> delegates;

    @Getter @Setter
    private WorldPoint selected;

    @Getter @Setter
    private boolean shown = true;

    @Getter @Setter
    private PainterVisibility visibilityMode;

    @Setter
    private boolean interactionEnabled;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    @Getter
    private float paintOpacity = 1.0f;

    @Getter
    private static Point2D correctedCursor = new Point2D.Double();

    AbstractPointPainter() {
        this.delegates = new ArrayList<>();
        this.delegateWorldToScreen = new AffineTransform();
        this.initChangeListeners();
        this.visibilityMode = PainterVisibility.SHOWN_WHEN_EDITED;
        this.setPaintOpacity(1.0f);
    }

    public static void initCursorListening() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                correctedCursor = checked.adjustedAndCorrected();
            }
        });
    }

    public boolean isInteractionEnabled() {
        return interactionEnabled;
    }

    void setPaintOpacity(float opacity) {
        if (opacity < 0.0f) {
            this.paintOpacity = 0.0f;
        } else this.paintOpacity = Math.min(opacity, 1.0f);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isMirrorable();

    @SuppressWarnings("OverlyComplexMethod")
    private void initChangeListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof PointRemoveQueued && this.isInteractionEnabled()) {
                if (selected == null) return;
                boolean mirroringEnabled = ControlPredicates.isMirrorModeEnabled();
                WorldPoint counterpart = null;
                if (isMirrorable() && mirroringEnabled) {
                    counterpart = getMirroredCounterpart(selected);
                }
                EditDispatch.postPointRemoved(this, (BaseWorldPoint) selected);
                if (counterpart != null) {
                    EditDispatch.postPointRemoved(this, (BaseWorldPoint) counterpart);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectQueued checked && this.isPointEligible(checked.point())) {
                if (!this.isInteractionEnabled()) return;
                this.handlePointSelectionEvent(checked.point());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointDragQueued checked) {
                if (!this.isInteractionEnabled()) return;
                if (selected == null) return;
                AffineTransform screenToWorld = checked.screenToWorld();
                Point2D translated = screenToWorld.transform(checked.adjustedCursor(), null);
                double x = translated.getX();
                double y = translated.getY();
                double roundedX = Math.round(x * 2) / 2.0;
                double roundedY = Math.round(y * 2) / 2.0;
                Point2D changedPosition = new Point2D.Double(roundedX, roundedY);

                WorldPoint counterpart = null;
                Point2D counterpartNewPosition = null;
                boolean mirroringEnabled = ControlPredicates.isMirrorModeEnabled();
                if (isMirrorable() && mirroringEnabled) {
                    counterpart = getMirroredCounterpart(this.selected);
                    if (counterpart != null) {
                        counterpartNewPosition = createCounterpartPosition(changedPosition);
                    }
                }
                EditDispatch.postPointDragged(this.selected, changedPosition);
                if (counterpartNewPosition != null) {
                    EditDispatch.postPointDragged(counterpart, counterpartNewPosition);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PainterOpacityChangeQueued checked) {
                Class<? extends AbstractPointPainter> painterClass = checked.painterClass();
                if (!painterClass.isInstance(this)) return;
                if (!isParentLayerActive()) return;
                this.setPaintOpacity(checked.change());
                EventBus.publish(new ViewerRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PainterVisibilityChanged checked) {
                Class<? extends AbstractPointPainter> painterClass = checked.painterClass();
                if (!painterClass.isInstance(this) || !isParentLayerActive()) return;
                this.setVisibilityMode(checked.changed());
                EventBus.publish(new ViewerRepaintQueued());
            }
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected abstract boolean isParentLayerActive();

    protected Point2D createCounterpartPosition(Point2D toMirror) {
        throw new UnsupportedOperationException("Point mirroring supported only for specific point painters!");
    }

    @SuppressWarnings("WeakerAccess")
    protected void handlePointSelectionEvent(WorldPoint point) {
        if (point != null) {
            if (this.selected != null) {
                this.selected.setSelected(false);
            }
            this.selected = point;
            this.selected.setSelected(true);
            EventBus.publish(new PointSelectedConfirmed(this.selected));
            EventBus.publish(new ViewerRepaintQueued());
        } else {
            selectPointConditionally();
        }
    }

    private void selectPointConditionally() {
        PointSelectionMode current = ControlPredicates.getSelectionMode();
        if (current == PointSelectionMode.STRICT) {
            this.selectPointStrictly();
            return;
        }
        Point2D cursor = AbstractPointPainter.getCorrectedCursor();
        WorldPoint toSelect = null;
        double minDistance = Double.MAX_VALUE;
        for (WorldPoint point : this.getPointsIndex()) {
            Point2D position = point.getPosition();
            double distance = position.distance(cursor);
            if (distance < minDistance) {
                minDistance = distance;
                toSelect = point;
            }
        }
        WorldPoint selectedPoint = this.getSelected();
        if (selectedPoint != null) {
            selectedPoint.setSelected(false);
        }
        this.setSelected(toSelect);
        if (toSelect != null) {
            toSelect.setSelected(true);
        }
        EventBus.publish(new PointSelectedConfirmed(toSelect));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private void selectPointStrictly() {
        if (!this.isMousedOverPoint()) return;
        if (this.selected != null) {
            this.selected.setSelected(false);
        }
        this.selected = this.getMousedOver();
        this.selected.setSelected(true);
        EventBus.publish(new PointSelectedConfirmed(this.selected));
        EventBus.publish(new ViewerRepaintQueued());


    }

    public abstract List<? extends BaseWorldPoint> getPointsIndex();

    protected abstract void addPointToIndex(BaseWorldPoint point);

    protected abstract void removePointFromIndex(BaseWorldPoint point);

    public abstract int getIndexOfPoint(BaseWorldPoint point);

    public abstract WorldPoint getMirroredCounterpart(WorldPoint point);

    protected abstract BaseWorldPoint getTypeReference();

    private boolean isPointEligible(WorldPoint point) {
        if (point != null) {
            BaseWorldPoint typeReference = getTypeReference();
            Class<? extends BaseWorldPoint> typeReferenceClass = typeReference.getClass();
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
        Painter painter = point.getPainter();
        this.delegates.add(painter);
    }

    public void removePoint(BaseWorldPoint point) {
        this.removePointFromIndex(point);
        EventBus.publish(new PointRemovedConfirmed(point));
        Painter painter = point.getPainter();
        if (this.selected == point) {
            this.setSelected(null);
        }
        this.delegates.remove(painter);
    }

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

    void paintDelegates(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.delegates.forEach(painter -> paintDelegate(g, worldToScreen, w, h, painter));
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
        if (visibility == PainterVisibility.SHOWN_WHEN_EDITED && !this.isInteractionEnabled()) return false;
        return isShown() || visibility == PainterVisibility.ALWAYS_SHOWN;
    }

    @Override
    public String toString() {
        Class<? extends AbstractPointPainter> identity = this.getClass();
        return identity.getSimpleName() + " @" + this.hashCode();
    }

}