package oth.shipeditor.utility;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundsPanelRepaintQueued;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
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
    private static PrimaryViewer viewer;

    @Getter @Setter
    private static ViewerLayer activeLayer;

    @Getter @Setter
    private static double rotationRadians;

    @Getter @Setter
    private static double rotationDegrees;

    @Getter @Setter
    private static double zoomLevel = 1;

    @Getter
    private static Point2D rawCursor = new Point2D.Double();

    @Getter
    private static Point2D adjustedCursor = new Point2D.Double();

    @Getter
    private static Point2D correctedCursor = new Point2D.Double();

    @Getter
    private static CoordsDisplayMode coordsMode = CoordsDisplayMode.WORLD;

    private StaticController() {
    }

    public static AffineTransform getScreenToWorld() {
        if (activeLayer != null) {
            LayerPainter painter = activeLayer.getPainter();
            if (painter != null) {
                AffineTransform worldToScreen = viewer.getWorldToScreen();
                return painter.getWithRotationInverse(worldToScreen);
            }
        }
        return viewer.getScreenToWorld();
    }

    public static AffineTransform getWorldToScreen() {
        return viewer.getWorldToScreen();
    }

    public static void updateViewerRotation(double radiansChange, double degrees) {
        rotationRadians += radiansChange;
        rotationDegrees = degrees;
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
                correctedCursor = checked.adjustedAndCorrected();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof CoordsModeChanged checked) {
                coordsMode = checked.newMode();
                EventBus.publish(new BoundsPanelRepaintQueued());
                EventBus.publish(new CenterPanelsRepaintQueued());
            }
        });
    }

}