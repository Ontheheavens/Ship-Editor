package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundPointPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.*;
import oth.shipeditor.components.entities.WorldPoint;

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

    protected final List<WorldPoint> pointsIndex;

    @Getter
    protected final List<Painter> delegates;

    @Getter @Setter
    private WorldPoint selected = null;

    @Getter @Setter
    private boolean interactionEnabled;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    protected final AffineTransform delegateWorldToScreen;

    public AbstractPointPainter() {
        this.delegates = new ArrayList<>();
        this.pointsIndex = new ArrayList<>();
        this.delegateWorldToScreen = new AffineTransform();
        this.initChangeListeners();
    }

    private void initChangeListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof PointRemoveQueued && interactionEnabled) {
                WorldPoint toRemove = this.getMousedOver();
                if (toRemove != null) {
                    this.removePoint(toRemove);
                    EventBus.publish(new ViewerRepaintQueued());
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectQueued checked && isPointEligible(checked.point())) {
                WorldPoint point = checked.point();
                if (point != null) {
                    if (selected != null) {
                        selected.setSelected(false);
                    }
                    selected = point;
                    selected.setSelected(true);
                    EventBus.publish(new PointSelectedConfirmed(selected));
                    EventBus.publish(new ViewerRepaintQueued());
                } else {
                    if (AbstractPointPainter.this.mousedOverPoint()) {
                        if (selected != null) {
                            selected.setSelected(false);
                        }
                        selected = AbstractPointPainter.this.getMousedOver();
                        selected.setSelected(true);
                        EventBus.publish(new PointSelectedConfirmed(selected));
                        EventBus.publish(new ViewerRepaintQueued());
                    } else if (selected != null) {
                        selected.setSelected(false);
                        selected = null;
                        EventBus.publish(new PointSelectedConfirmed(null));
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointDragQueued checked) {
                if (selected == null) return;
                Point2D translated = checked.screenToWorld().transform(checked.adjustedCursor(), null);
                double roundedX = Math.round(translated.getX() * 2) / 2.0;
                double roundedY = Math.round(translated.getY() * 2) / 2.0;
                selected.movePosition(roundedX, roundedY);
                EventBus.publish(new ViewerRepaintQueued());
                EventBus.publish(new BoundPointPanelRepaintQueued());
            }
        });
    }

    protected abstract List<? extends WorldPoint> getPointsIndex();

    protected abstract WorldPoint getTypeReference();

    protected boolean isPointEligible(WorldPoint point) {
        if (point != null) {
            return getTypeReference().getClass().isAssignableFrom(point.getClass());
        }
        return true;
    }

    private boolean mousedOverPoint() {
        return this.getMousedOver() != null;
    }

    private WorldPoint getMousedOver() {
        WorldPoint mousedOver = null;
        for (WorldPoint point : this.getPointsIndex()) {
            if (point.isCursorInBounds()) {
                mousedOver = point;
            }
        }
        return mousedOver;
    }

    public void addPoint(WorldPoint point) {
        pointsIndex.add(point);
        EventBus.publish(new PointAddConfirmed(point));
        delegates.add(point.getPainter());
    }

    public void removePoint(WorldPoint point) {
        pointsIndex.remove(point);
        EventBus.publish(new PointRemovedConfirmed(point));
        delegates.remove(point.getPainter());
    }

    public boolean pointAtCoordsExists(Point2D point2D) {
        boolean pointDoesExist = false;
        for (WorldPoint point : pointsIndex) {
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
        for (Painter delegate : delegates)
        {
            if (delegate != null)
            {
                delegateWorldToScreen.setTransform(worldToScreen);
                delegate.paint(g, delegateWorldToScreen, w, h);
            }
        }
    }

}
