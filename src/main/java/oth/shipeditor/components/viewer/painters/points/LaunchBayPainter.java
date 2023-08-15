package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.communication.events.viewer.points.LaunchBayAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.LaunchBayRemoveConfirmed;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
@Log4j2
public class LaunchBayPainter extends MirrorablePointPainter {

    private final List<LaunchPortPoint> portsIndex;

    @Getter
    private final List<LaunchBay> baysList;

    public LaunchBayPainter(ShipPainter parent) {
        super(parent);
        this.portsIndex = new ArrayList<>();
        this.baysList = new ArrayList<>();

        this.initModeListener();
        this.setInteractionEnabled(ShipInstrumentsPane.getCurrentMode() == ShipInstrument.LAUNCH_BAYS);
    }

    private void initModeListener() {
        List<BusEventListener> listeners = getListeners();
        BusEventListener modeListener = event -> {
            if (event instanceof InstrumentModeChanged checked) {
                setInteractionEnabled(checked.newMode() == ShipInstrument.LAUNCH_BAYS);
            }
        };
        listeners.add(modeListener);
        EventBus.subscribe(modeListener);
    }

    @Override
    public List<LaunchPortPoint> getPointsIndex() {
        return portsIndex;
    }

    public void addBay(LaunchBay bay) {
        baysList.add(bay);
        EventBus.publish(new LaunchBayAddConfirmed(bay));
    }

    private void removeBay(LaunchBay bay) {
        baysList.remove(bay);
        EventBus.publish(new LaunchBayRemoveConfirmed(bay));
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof LaunchPortPoint checked) {
            LaunchBay targetBay = checked.getParentBay();
            if (!baysList.contains(targetBay)) {
                this.addBay(targetBay);
            }
            List<LaunchPortPoint> portPoints = targetBay.getPortPoints();
            portPoints.add(checked);
            portsIndex.add(checked);
        } else {
            throw new IllegalArgumentException("Attempted to add incompatible point to LaunchBayPainter!");
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof LaunchPortPoint checked) {
            portsIndex.remove(checked);
            LaunchBay parentBay = checked.getParentBay();
            List<LaunchPortPoint> portPoints = parentBay.getPortPoints();
            portPoints.remove(checked);
            if (portPoints.isEmpty()) {
                this.removeBay(parentBay);
            }
        } else {
            throw new IllegalArgumentException("Attempted to remove incompatible point from LaunchBayPainter!");
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof LaunchPortPoint checked) {
            return portsIndex.indexOf(checked);
        } else {
            throw new IllegalArgumentException("Attempted to access incompatible point in LaunchBayPainter!");
        }
    }

    @Override
    protected Class<? extends BaseWorldPoint> getTypeReference() {
        return LaunchPortPoint.class;
    }

    @Override
    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {
        throw new UnsupportedOperationException("Point insertion unsupported for LaunchBayPainter!");
    }

    @Override
    public void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Point2D finalWorldCursor = StaticController.getFinalWorldCursor();
        Point2D finalScreenCursor = worldToScreen.transform(finalWorldCursor, null);
        WorldPoint selected = this.getSelected();
        if (selected != null && isInteractionEnabled()) {
            Point2D selectedPosition = worldToScreen.transform(selected.getPosition(), null);
            DrawUtilities.drawScreenLine(g, selectedPosition, finalScreenCursor, Color.BLACK, 4.0f);
            DrawUtilities.drawScreenLine(g, selectedPosition, finalScreenCursor, Color.LIGHT_GRAY, 2.0f);
        }


    }

}
