package oth.shipeditor.undo;

import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.control.ViewerMouseReleased;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.entities.*;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.MirrorablePointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.points.ship.EngineSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.edits.*;
import oth.shipeditor.undo.edits.features.*;
import oth.shipeditor.undo.edits.points.*;
import oth.shipeditor.undo.edits.points.engines.*;
import oth.shipeditor.undo.edits.points.slots.*;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Convenience class meant to free viewer classes from burden of also implementing all the edit dispatch methods.
 * @author Ontheheavens
 * @since 16.06.2023
 */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyMethods"})
public final class EditDispatch {

    private EditDispatch() {
    }

    private static void handleContinuousEdit(Edit edit) {
        Class<? extends Edit> editClass = edit.getClass();
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (editClass.isInstance(previousEdit) && !previousEdit.isFinished()) {
            edit.setFinished(true);
            previousEdit.add(edit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !edit.isFinished()) {
                        edit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(edit);
        }
    }

    public static void postPointInserted(MirrorablePointPainter pointPainter, BoundPoint point, int index) {
        Edit addEdit = new PointAdditionEdit(pointPainter, point, index);
        UndoOverseer.post(addEdit);
        pointPainter.insertPoint(point, index);
        Events.repaintShipView();
    }

    public static void postBoundsRearranged(BoundPointsPainter pointPainter,
                                            List<BoundPoint> old,
                                            List<BoundPoint> changed) {
        Edit rearrangeEdit = new BoundsSortEdit(pointPainter, old, changed);
        UndoOverseer.post(rearrangeEdit);
        pointPainter.setBoundPoints(changed);
        Events.repaintShipView();
    }

    public static void postSlotsRearranged(WeaponSlotPainter pointPainter,
                                           List<WeaponSlotPoint> old,
                                           List<WeaponSlotPoint> changed) {
        Edit rearrangeEdit = new WeaponSlotsSortEdit(pointPainter, old, changed);
        UndoOverseer.post(rearrangeEdit);
        pointPainter.setSlotPoints(changed);
        var repainter = StaticController.getRepainter();
        repainter.queueSlotControlRepaint();
    }

    public static void postEnginesRearranged(EngineSlotPainter pointPainter,
                                             List<EnginePoint> old,
                                             List<EnginePoint> changed) {
        Edit rearrangeEdit = new EnginesSortEdit(pointPainter, old, changed);
        UndoOverseer.post(rearrangeEdit);
        pointPainter.setEnginePoints(changed);
        var repainter = StaticController.getRepainter();
        repainter.queueEnginesPanelRepaint();
    }

    public static void postPointAdded(AbstractPointPainter pointPainter, BaseWorldPoint point) {
        Edit addEdit = new PointAdditionEdit(pointPainter, point);
        UndoOverseer.post(addEdit);
        pointPainter.addPoint(point);
        Events.repaintShipView();
    }

    public static void postPointRemoved(AbstractPointPainter pointPainter, BaseWorldPoint point) {
        int index = pointPainter.getIndexOfPoint(point);
        if (index == -1) return;
        Edit removeEdit = new PointRemovalEdit(pointPainter, point, index);
        UndoOverseer.post(removeEdit);
        pointPainter.removePoint(point);
        Events.repaintShipView();
    }

    public static void postAnchorOffsetChanged(LayerPainter layerPainter, Point2D updated) {
        Point2D oldOffset = layerPainter.getAnchor();
        Edit offsetChangeEdit = new AnchorOffsetEdit(layerPainter, oldOffset, updated);
        EditDispatch.handleContinuousEdit(offsetChangeEdit);
        layerPainter.setAnchor(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBoundsPanelRepaint();
        repainter.queueCenterPanelsRepaint();
        repainter.queueBuiltInsRepaint();
    }

    public static void postSlotAngleSet(SlotData slotPoint, double old, double updated ) {
        Edit angleEdit = new SlotAngleSet(slotPoint, old, updated);
        EditDispatch.handleContinuousEdit(angleEdit);
        slotPoint.setAngle(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    public static void postEngineAngleSet(EnginePoint enginePoint, double old, double updated ) {
        Edit angleEdit = new EngineAngleSet(enginePoint, old, updated);
        EditDispatch.handleContinuousEdit(angleEdit);
        enginePoint.setAngle(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    public static void postEngineSizeChanged(EnginePoint enginePoint, Size2D updated) {
        Size2D oldSize = enginePoint.getSize();
        Edit sizeEdit = new EngineSizeSet(enginePoint, oldSize, updated);
        EditDispatch.handleContinuousEdit(sizeEdit);
        enginePoint.setSize(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    public static void postEngineContrailChanged(EnginePoint enginePoint, int updated) {
        int oldContrail = (int) enginePoint.getContrailSize();
        Edit contrailEdit = new EngineContrailSet(enginePoint, oldContrail, updated);
        EditDispatch.handleContinuousEdit(contrailEdit);
        enginePoint.setContrailSize(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    public static void postEngineStyleChanged(EnginePoint enginePoint, EngineStyle updated) {
        EngineStyle oldStyle = enginePoint.getStyle();
        Edit styleEdit = new EngineStyleSet(enginePoint, oldStyle, updated);
        UndoOverseer.post(styleEdit);
        enginePoint.setStyle(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueEnginesPanelRepaint();
    }

    public static void postSlotArcSet(SlotData slotPoint, double old, double updated ) {
        Edit arcEdit = new SlotArcSet(slotPoint, old, updated);
        EditDispatch.handleContinuousEdit(arcEdit);
        slotPoint.setArc(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    public static void postLayerRotated(LayerPainter painter, double old, double updated) {
        Edit rotationEdit = new LayerRotationEdit(painter, old, updated);
        EditDispatch.handleContinuousEdit(rotationEdit);
        painter.setRotationRadians(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBoundsPanelRepaint();
        repainter.queueCenterPanelsRepaint();
        repainter.queueBuiltInsRepaint();
    }

    public static void postPointDragged(WorldPoint selected, Point2D changedPosition) {
        Point2D position = selected.getPosition();
        Point2D wrappedOld = new Point2D.Double(position.getX(), position.getY());
        Point2D wrappedNew = new Point2D.Double(changedPosition.getX(), changedPosition.getY());
        Edit dragEdit = new PointDragEdit(selected, wrappedOld, wrappedNew);
        EditDispatch.handleContinuousEdit(dragEdit);
        selected.setPosition(changedPosition);
        PointDragEdit.repaintByPointType(selected);
    }

    public static void postCollisionRadiusChanged(ShipCenterPoint point, float radius) {
        float oldRadius = point.getCollisionRadius();
        Edit radiusEdit = new CollisionRadiusEdit(point, oldRadius, radius);
        EditDispatch.handleContinuousEdit(radiusEdit);
        point.setCollisionRadius(radius);
        Events.repaintShipView();
    }

    public static void postShieldRadiusChanged(ShieldCenterPoint point, float radius) {
        float oldRadius = point.getShieldRadius();
        Edit radiusEdit = new ShieldRadiusEdit(point, oldRadius, radius);
        EditDispatch.handleContinuousEdit(radiusEdit);
        point.setShieldRadius(radius);
        Events.repaintShipView();
    }

    public static void postSlotIDChanged(SlotData point, String newID) {
        String oldID = point.getId();
        Edit renameEdit = new SlotIDChangeEdit(point, newID, oldID);
        UndoOverseer.post(renameEdit);
        point.changeSlotID(newID);
        var repainter = StaticController.getRepainter();
        repainter.queueSlotControlRepaint();
        repainter.queueBuiltInsRepaint();
    }

    public static void postSlotTypeChanged(SlotData point, WeaponType newType) {
        WeaponType oldType = point.getWeaponType();
        Edit typeChangeEdit = new SlotTypeChangeEdit(point, oldType, newType);
        UndoOverseer.post(typeChangeEdit);
        point.setWeaponType(newType);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBuiltInsRepaint();
    }

    public static void postSlotMountChanged(SlotData point, WeaponMount newMount) {
        WeaponMount oldMount = point.getWeaponMount();
        Edit mountChangeEdit = new SlotMountChangeEdit(point, oldMount, newMount);
        UndoOverseer.post(mountChangeEdit);
        point.setWeaponMount(newMount);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
    }

    public static void postSlotSizeChanged(SlotData point, WeaponSize newSize) {
        WeaponSize oldSize = point.getWeaponSize();
        Edit sizeChangeEdit = new SlotSizeChangeEdit(point, oldSize, newSize);
        UndoOverseer.post(sizeChangeEdit);
        point.setWeaponSize(newSize);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueBaysPanelRepaint();
    }

    public static void postLaunchPortsRearranged(LaunchPortPoint portPoint, LaunchBay targetBay, int targetIndex) {
        LaunchBay oldParent = portPoint.getParentBay();

        var oldParentPorts = oldParent.getPortPoints();
        int oldIndex = oldParentPorts.indexOf(portPoint);

        LaunchPortsSortEdit portsSortEdit = new LaunchPortsSortEdit(portPoint, targetBay, oldParent,
                targetIndex, oldIndex);
        UndoOverseer.post(portsSortEdit);

        portsSortEdit.transferPort(oldParent, targetBay, targetIndex);
    }

    public static void postHullmodAdded(List<HullmodCSVEntry> index, ShipLayer shipLayer, HullmodCSVEntry hullmod) {
        Edit hullmodAddEdit = new HullmodAddEdit(index, shipLayer, hullmod);
        UndoOverseer.post(hullmodAddEdit);
        index.add(hullmod);
    }

    public static void postHullmodRemoved(List<HullmodCSVEntry> index, ShipLayer shipLayer, HullmodCSVEntry hullmod) {
        Edit hullmodAddEdit = new HullmodRemoveEdit(index, shipLayer, hullmod);
        UndoOverseer.post(hullmodAddEdit);
        index.remove(hullmod);
    }

    public static void postWingAdded(List<WingCSVEntry> index, ShipLayer shipLayer, WingCSVEntry wing) {
        Edit wingAddEdit = new WingAddEdit(index, shipLayer, wing);
        UndoOverseer.post(wingAddEdit);
        index.add(wing);
    }

    public static void postWingRemoved(List<WingCSVEntry> index, ShipLayer shipLayer, WingCSVEntry wing) {
        Edit wingRemoveEdit = new WingRemoveEdit(index, shipLayer, wing);
        UndoOverseer.post(wingRemoveEdit);
        index.remove(wing);
    }

    public static<T extends InstallableEntry> void postFeatureInstalled(Map<String, T> collection,
                                                                          String slotID, T feature,
                                                                          Runnable afterAction) {
        // Theoretically should not need order preservation since added is always last.
        Edit installEdit = new FeatureInstallEdit<>(collection, slotID, feature, afterAction);
        UndoOverseer.post(installEdit);
        collection.put(slotID, feature);
        if (afterAction != null) {
            afterAction.run();
        }
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueVariantsRepaint();
    }

    public static<T extends InstallableEntry> void postFeatureUninstalled(Map<String, T> collection,
                                                                          String slotID, T feature,
                                                                          Runnable invalidator) {
        // Note: this technique of manipulating collection state instead of explicit removal/re-adding
        // assumes that order of operations is strictly preserved in edit stack.
        // Should the sequence be broken, this will backfire.
        Map<String, T> before = new LinkedHashMap<>(collection);
        collection.remove(slotID, feature);
        Map<String, T> after = new LinkedHashMap<>(collection);
        Edit uninstallEdit = new FeatureUninstallEdit<>(before, after, collection, invalidator);
        UndoOverseer.post(uninstallEdit);
        if (invalidator != null) {
            invalidator.run();
        }
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueVariantsRepaint();
    }

    public static<T extends InstallableEntry> void postBuiltInFeaturesSorted(Consumer<Map<String, T>> consumer,
                                                                             Map<String, T> oldMap,
                                                                             Map<String, T> newMap) {
        Edit sortEdit = new FeatureSortEdit<>(consumer, oldMap, newMap);
        UndoOverseer.post(sortEdit);
        consumer.accept(newMap);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }

    public static void postWeaponGroupsRearranged(InstalledFeature feature, FittedWeaponGroup targetGroup,
                                                  int targetIndex) {
        FittedWeaponGroup oldParent = feature.getParentGroup();

        var oldParentWeapons = oldParent.getWeapons();
        int oldIndex = oldParentWeapons.indexOf(feature.getSlotID());

        Edit groupsSortEdit = new WeaponGroupsSortEdit(feature, targetGroup, oldParent,
                targetIndex, oldIndex);
        UndoOverseer.post(groupsSortEdit);

        groupsSortEdit.redo();
    }

    public static void postModulesSorted(Consumer<Map<String, InstalledFeature>> moduleSetter,
                                                                             Map<String, InstalledFeature> oldMap,
                                                                             Map<String, InstalledFeature> newMap) {
        Edit sortEdit = new ModulesSortEdit(moduleSetter, oldMap, newMap);
        UndoOverseer.post(sortEdit);
        moduleSetter.accept(newMap);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueModulesRepaint();
    }

}
