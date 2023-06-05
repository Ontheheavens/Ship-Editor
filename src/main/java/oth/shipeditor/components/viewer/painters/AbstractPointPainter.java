package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundPointPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
@Log4j2
public abstract class AbstractPointPainter implements Painter {

    // TODO: Implement mirror mode.

    @Getter
    protected final List<Painter> delegates;

    @Getter @Setter
    private WorldPoint selected;

    @Getter @Setter
    private boolean interactionEnabled;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    protected AbstractPointPainter() {
        this.delegates = new ArrayList<>();
        this.delegateWorldToScreen = new AffineTransform();
        this.initChangeListeners();
    }

    private void initChangeListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof PointRemoveQueued && this.interactionEnabled) {
                BaseWorldPoint toRemove = this.getMousedOver();
                if (toRemove != null) {
                    this.removePoint(toRemove);
                    EventBus.publish(new ViewerRepaintQueued());
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectQueued checked && this.isPointEligible(checked.point())) {
                this.handlePointSelectionEvent(checked.point());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointDragQueued checked) {
                if (selected == null) return;
                AffineTransform screenToWorld = checked.screenToWorld();
                Point2D translated = screenToWorld.transform(checked.adjustedCursor(), null);
                double x = translated.getX();
                double y = translated.getY();
                double roundedX = Math.round(x * 2) / 2.0;
                double roundedY = Math.round(y * 2) / 2.0;
                selected.setPosition(roundedX, roundedY);
                EventBus.publish(new ViewerRepaintQueued());
                EventBus.publish(new BoundPointPanelRepaintQueued());
            }
        });
    }

    private void handlePointSelectionEvent(WorldPoint point) {
        if (point != null) {
            if (this.selected != null) {
                this.selected.setSelected(false);
            }
            this.selected = point;
            this.selected.setSelected(true);
            EventBus.publish(new PointSelectedConfirmed(this.selected));
            EventBus.publish(new ViewerRepaintQueued());
        } else {
            if (this.isMousedOverPoint()) {
                if (this.selected != null) {
                    this.selected.setSelected(false);
                }
                this.selected = this.getMousedOver();
                this.selected.setSelected(true);
                EventBus.publish(new PointSelectedConfirmed(this.selected));
                EventBus.publish(new ViewerRepaintQueued());
            } else if (this.selected != null) {
                this.selected.setSelected(false);
                this.selected = null;
                EventBus.publish(new PointSelectedConfirmed(null));
                EventBus.publish(new ViewerRepaintQueued());
            }
        }
    }

    protected abstract List<? extends BaseWorldPoint> getPointsIndex();

    protected abstract void addPointToIndex(BaseWorldPoint point);

    protected abstract void removePointFromIndex(BaseWorldPoint point);

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

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        for (Painter delegate : this.delegates)
        {
            if (delegate != null)
            {
                this.delegateWorldToScreen.setTransform(worldToScreen);
                delegate.paint(g, this.delegateWorldToScreen, w, h);
            }
        }
    }

    @Override
    public String toString() {
        Class<? extends AbstractPointPainter> identity = this.getClass();
        return identity.getSimpleName() + " @" + this.hashCode();
    }

}
