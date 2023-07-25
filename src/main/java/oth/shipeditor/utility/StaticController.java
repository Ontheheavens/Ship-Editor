package oth.shipeditor.utility;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.PrimaryShipViewer;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.menubar.FileUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Convenience class for static access to active layer and whatever other global features need to be accessed.
 * @author Ontheheavens
 * @since 09.07.2023
 */
public final class StaticController {

    @Setter
    private static PrimaryShipViewer shipViewer;

    @Getter @Setter
    private static ShipLayer activeLayer;

    @Getter @Setter
    private static double rotationRadians;

    @Getter @Setter
    private static double zoomLevel = 1;

    @Getter @Setter
    private static Point2D rawCursor = new Point2D.Double();

    @Getter @Setter
    private static Point2D adjustedCursor = new Point2D.Double();

    private StaticController() {
    }

    public static AffineTransform getScreenToWorld() {
        return shipViewer.getScreenToWorld();
    }

    public static void updateRotationRadians(double input) {
        rotationRadians += input;
    }

    public static boolean checkIsHovered(Shape shape) {
        return shape.contains(rawCursor);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                activeLayer = checked.selected();
                FileUtilities.updateActionStates(activeLayer);
            }
            if (event instanceof ActiveLayerUpdated checked) {
                activeLayer = checked.updated();
                FileUtilities.updateActionStates(activeLayer);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                rawCursor = checked.rawCursor();
                adjustedCursor = checked.adjusted();
            }
        });
    }

}
