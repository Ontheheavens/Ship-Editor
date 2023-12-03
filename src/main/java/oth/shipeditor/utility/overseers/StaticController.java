package oth.shipeditor.utility.overseers;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.ship.HullSize;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Convenience class for static access to active layer and whatever other global features need to be accessed.
 * @author Ontheheavens
 * @since 09.07.2023
 */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyMethods"})
public final class StaticController {

    @Getter @Setter
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
    private static final EventScheduler scheduler = new EventScheduler();

    @Getter
    private static CoordsDisplayMode coordsMode = CoordsDisplayMode.SHIP_CENTER;

    private StaticController() {
    }

    public static LayerManager getLayerManager() {
        if (viewer != null) {
            return viewer.getLayerManager();
        }
        return null;
    }

    /**
     * Is used as a shortcut to refresh UI for respective ship editing panels. It's not an optimal practice!
     */
    public static void reselectCurrentLayer() {
        LayerManager manager = viewer.getLayerManager();
        var current = manager.getActiveLayer();
        manager.setActiveLayer(current);
    }

    public static Point2D getCorrectedWithoutRotate() {
        Point2D currentCursor = StaticController.getAdjustedCursor();
        AffineTransform screenToWorld = viewer.getScreenToWorld();
        return Utility.correctAdjustedCursor(currentCursor, screenToWorld);
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

    public static EditorInstrument getEditorMode() {
        return ShipInstrumentsPane.getCurrentMode();
    }

    public static Point2D getFinalWorldCursor() {
        AffineTransform screenToWorld = StaticController.getScreenToWorld();
        Point2D finalWorldCursor = screenToWorld.transform(StaticController.getRawCursor(), null);
        if (ControlPredicates.isCursorSnappingEnabled()) {
            Point2D cursor = StaticController.getAdjustedCursor();
            finalWorldCursor = Utility.correctAdjustedCursor(cursor, screenToWorld);
        }
        return finalWorldCursor;
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

    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected) {
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
                EventBus.publish(new InstrumentRepaintQueued(EditorInstrument.BOUNDS));
                EventBus.publish(new InstrumentRepaintQueued(EditorInstrument.COLLISION));
                EventBus.publish(new InstrumentRepaintQueued(EditorInstrument.SHIELD));
            }
        });
    }

    public static void actOnCurrentVariant(BiConsumer<ShipLayer, ShipVariant> action) {
        if (activeLayer instanceof ShipLayer shipLayer) {
            var shipPainter = shipLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return;
            var variant = shipPainter.getActiveVariant();
            if (variant == null || variant.isEmpty()) return;

            action.accept(shipLayer, variant);
        }
    }

    public static void actOnCurrentSkin(BiConsumer<ShipLayer, ShipSkin> action) {
        if (activeLayer instanceof ShipLayer shipLayer) {
            var shipPainter = shipLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return;
            var skin = shipPainter.getActiveSkin();
            if (skin == null || skin.isBase()) return;

            action.accept(shipLayer, skin);
        }
    }

    public static void actOnCurrentShip(Consumer<ShipLayer> action) {
        if (activeLayer instanceof ShipLayer shipLayer) {
            var shipPainter = shipLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return;
            action.accept(shipLayer);
        }
    }

    public static HullSize getSizeOfActiveLayer() {
        HullSize size = null;

        var layer = StaticController.getActiveLayer();
        if (layer instanceof ShipLayer shipLayer) {
            ShipHull shipHull = shipLayer.getHull();
            if (shipHull != null) {
                size = shipHull.getHullSize();
            }
        }

        return size;
    }

    public static WeaponSlotPainter getSelectedSlotPainter() {
        var viewerLayer = StaticController.getActiveLayer();
        if (viewerLayer instanceof ShipLayer shipLayer) {
            var shipPainter = shipLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return null;
            return shipPainter.getWeaponSlotPainter();
        }
        return null;
    }

    public static boolean isShipLayerActive() {
        ViewerLayer viewerLayer = StaticController.getActiveLayer();
        if (viewerLayer == null) {
            return false;
        }
        if (viewerLayer.getPainter() instanceof ShipPainter shipPainter) {
            return !shipPainter.isUninitialized();
        }
        return false;
    }

    /**
     * @return selected slot from a currently active layer, with instrument mode eligibility checks.
     */
    public static WeaponSlotPoint getSelectedAndEligibleSlot() {
        ViewerLayer viewerLayer = StaticController.getActiveLayer();
        if (viewerLayer instanceof ShipLayer shipLayer) {
            var shipPainter = shipLayer.getPainter();
            return Utility.getSelectedFromLayer(shipPainter);
        }
        return null;
    }

}
