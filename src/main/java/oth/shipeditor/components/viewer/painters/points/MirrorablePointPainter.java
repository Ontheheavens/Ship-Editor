package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
public abstract class MirrorablePointPainter extends AbstractPointPainter {

    @Getter
    private final LayerPainter parentLayer;

    MirrorablePointPainter(LayerPainter parent) {
        this.parentLayer = parent;
        initModeListener();
        this.setInteractionEnabled(StaticController.getEditorMode() == getInstrumentType());
    }

    void initInteractionListeners() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener slotCreationListener = event -> {
            if (event instanceof PointCreationQueued checked) {
                if (!isInteractionEnabled()) return;
                if (!hasPointAtCoords(checked.position())) {
                    this.handleCreation(checked);
                }
            }
        };
        listeners.add(slotCreationListener);
        EventBus.subscribe(slotCreationListener);
    }

    private void initModeListener() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener modeListener = event -> {
            if (event instanceof InstrumentModeChanged checked) {
                setInteractionEnabled(checked.newMode() == getInstrumentType());
            }
        };
        listeners.add(modeListener);
        EventBus.subscribe(modeListener);
    }

    protected abstract EditorInstrument getInstrumentType();

    protected abstract void handleCreation(PointCreationQueued event);

    @Override
    protected boolean isParentLayerActive() {
        return this.parentLayer.isLayerActive();
    }

    @Override
    protected Point2D createCounterpartPosition(Point2D toMirror) {
        Point2D entityCenter = parentLayer.getEntityCenter();
        double counterpartX = 2 * entityCenter.getX() - toMirror.getX();
        double counterpartY = toMirror.getY(); // Y-coordinate remains the same.
        return new Point2D.Double(counterpartX, counterpartY);
    }

    @Override
    protected boolean checkVisibility() {
        PainterVisibility visibilityMode = getVisibilityMode();
        boolean parentCheck = super.checkVisibility();
        if (visibilityMode == PainterVisibility.SHOWN_WHEN_SELECTED && !parentLayer.isLayerActive()) return false;
        return parentCheck;
    }

    @Override
    public boolean isMirrorable() {
        return true;
    }

    public abstract void insertPoint(BaseWorldPoint toInsert, int precedingIndex);

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!checkVisibility()) return;

        float alpha = this.getPaintOpacity();
        Composite old = Utility.setAlphaComposite(g, alpha);

        this.paintPainterContent(g, worldToScreen, w, h);

        this.handleSelectionHighlight();
        this.paintDelegates(g, worldToScreen, w, h);
        g.setComposite(old);
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {}

    void handleSelectionHighlight() {
        WorldPoint selection = this.getSelected();
        if (selection != null && isInteractionEnabled()) {
            MirrorablePointPainter.enlargePoint(selection);
            this.actOnCounterpart(MirrorablePointPainter::enlargePoint, selection);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends WorldPoint> void actOnCounterpart(Consumer<T> action, T point) {
        boolean mirrorMode = ControlPredicates.isMirrorModeEnabled();
        BaseWorldPoint mirroredCounterpart = getMirroredCounterpart(point);
        Class<? extends WorldPoint> pointClass = point.getClass();
        if (mirrorMode && pointClass.isInstance(mirroredCounterpart)) {
            T checkedCounterpart = (T) pointClass.cast(mirroredCounterpart);
            action.accept(checkedCounterpart);
        }
    }

    private static void enlargePoint(WorldPoint point) {
        point.setPaintSizeMultiplier(1.5);
    }

    @Override
    void paintDelegates(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        super.paintDelegates(g, worldToScreen, w, h);
        for (BaseWorldPoint point : getPointsIndex()) {
            point.setPaintSizeMultiplier(1);
        }
    }

    @Override
    public BaseWorldPoint getMirroredCounterpart(WorldPoint inputPoint) {
        Point2D pointPosition = inputPoint.getPosition();
        Point2D counterpartPosition = this.createCounterpartPosition(pointPosition);
        BaseWorldPoint closestPoint = this.findClosestPoint(counterpartPosition);
        double threshold = ControlPredicates.getMirrorPointLinkageTolerance();

        if (closestPoint != null) {
            double closestDistance = counterpartPosition.distance(closestPoint.getPosition());
            if (closestDistance <= threshold) {
                return closestPoint;
            }
        }

        return null;
    }

}
