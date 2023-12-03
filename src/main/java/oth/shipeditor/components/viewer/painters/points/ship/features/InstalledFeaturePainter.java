package oth.shipeditor.components.viewer.painters.points.ship.features;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@Getter @Setter
@Log4j2
public final class InstalledFeaturePainter {

    private PainterVisibility builtInsVisibility;

    private PainterVisibility decorativesVisibility;

    private WeaponSlotPoint cachedSelectCounterpart;

    /**
     * Has magic numbers: Integer.MIN_VALUE is for modules with UNDER_PARENT tag,
     * same number + 1 is for normal modules.
     */
    private Map<Integer, Set<InstalledFeature>> orderedRenderQueue;

    public InstalledFeaturePainter() {
        this.builtInsVisibility = PainterVisibility.ALWAYS_SHOWN;
        this.decorativesVisibility = PainterVisibility.ALWAYS_SHOWN;
    }

    private static boolean checkVisibility(ShipPainter painter, PainterVisibility visibility,
                                           EditorInstrument featureKind) {
        boolean layerActive = painter.isLayerActive();
        if (visibility == PainterVisibility.ALWAYS_HIDDEN) return false;

        EditorInstrument editorMode = StaticController.getEditorMode();

        boolean eligible = editorMode == featureKind && painter.isLayerActive();
        if (visibility == PainterVisibility.SHOWN_WHEN_EDITED && eligible) return true;
        if (visibility == PainterVisibility.SHOWN_WHEN_SELECTED && layerActive) return true;
        return visibility == PainterVisibility.ALWAYS_SHOWN;
    }

    private static boolean isInteractable(ShipPainter painter) {
        EditorInstrument editorMode = StaticController.getEditorMode();
        boolean eligibleMode = editorMode == EditorInstrument.BUILT_IN_WEAPONS
                || editorMode == EditorInstrument.DECORATIVES
                || editorMode == EditorInstrument.VARIANT_DATA;
        return eligibleMode && painter.isLayerActive();
    }

    public void updateRenderQueue(ShipPainter painter) {
        Map<Integer, Set<InstalledFeature>> result = new TreeMap<>();

        var slotPainter = painter.getWeaponSlotPainter();

        Map<String, InstalledFeature> toPrepare = new LinkedHashMap<>();

        var decoratives = painter.getBuiltInsWithSkin(true, false);
        if (InstalledFeaturePainter.checkVisibility(painter, this.decorativesVisibility,
                EditorInstrument.DECORATIVES)) {
            toPrepare.putAll(decoratives);
        }

        var builtIns = painter.getBuiltInsWithSkin(false, true);
        if (InstalledFeaturePainter.checkVisibility(painter, this.builtInsVisibility,
                EditorInstrument.BUILT_IN_WEAPONS)) {
            builtIns.forEach(toPrepare::putIfAbsent);
        }

        var builtInModules = painter.getBuiltInModules();
        if (builtInModules != null) {
            builtInModules.forEach(toPrepare::putIfAbsent);
        }

        ShipVariant shipVariant = painter.getActiveVariant();
        if (shipVariant != null && !shipVariant.isEmpty()) {
            var modules = shipVariant.getFittedModules();
            if (modules != null) {
                modules.forEach(toPrepare::putIfAbsent);
            }
            var allWeapons = shipVariant.getAllFittedWeapons();
            if (allWeapons != null) {
                allWeapons.forEach(toPrepare::putIfAbsent);
            }
        }

        toPrepare.forEach((slotID, feature) -> this.prepareFeature(result, slotPainter, slotID, feature));

        if (cachedSelectCounterpart != null && ControlPredicates.isMirrorModeEnabled()) {
            result.forEach((integer, installedFeatures) -> installedFeatures.forEach(feature -> {
                String slotID = feature.getSlotID();
                if (cachedSelectCounterpart != null && slotID.equals(cachedSelectCounterpart.getId())) {
                    LayerPainter featurePainter = feature.getFeaturePainter();
                    featurePainter.setSpriteOpacity(0.75f);
                    cachedSelectCounterpart = null;
                }
            }));
            cachedSelectCounterpart = null;
        }

        this.orderedRenderQueue = result;
    }

    private void prepareFeature(Map<Integer, Set<InstalledFeature>> collection,
                                WeaponSlotPainter slotPainter, String slotID,
                                InstalledFeature feature) {
        int renderOrder = this.refreshSlotData(slotPainter, slotID, feature);
        if (renderOrder == Integer.MAX_VALUE) return;
        var renderLayer = collection.computeIfAbsent(renderOrder,
                k -> new LinkedHashSet<>());
        renderLayer.add(feature);
        collection.put(renderOrder, renderLayer);
    }

    /**
     * @return integer's max value as a magic number if the respective slot is not found or invalid.
     */
    private int refreshSlotData(WeaponSlotPainter slotPainter, String slotID,
                                InstalledFeature feature) {
        WeaponSlotPoint slotPoint = slotPainter.getSlotByID(slotID);

        LayerPainter installablePainter = feature.getFeaturePainter();
        if (slotPoint == null || !slotPoint.canFit(feature)) {
            feature.setInvalidated(true);
            return Integer.MAX_VALUE;
        }
        if (slotPoint.isPointSelected() && InstalledFeaturePainter.isInteractable(slotPainter.getParentLayer())) {
            installablePainter.setSpriteOpacity(0.75f);
            cachedSelectCounterpart = (WeaponSlotPoint) slotPainter.getMirroredCounterpart(slotPoint);
        } else {
            installablePainter.setSpriteOpacity(1.0f);
        }

        feature.setInvalidated(false);
        int renderOrder = feature.computeRenderOrder(slotPoint);
        feature.refreshPaintCircumstance(slotPoint);
        return renderOrder;
    }

    public void paintUnderParent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.paintFeatures(g, worldToScreen, w, h, integer -> integer == Integer.MIN_VALUE);
    }

    public void paintNormal(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.paintFeatures(g, worldToScreen, w, h, integer -> integer != Integer.MIN_VALUE);
    }

    private void paintFeatures(Graphics2D g, AffineTransform worldToScreen, double w, double h,
                               Predicate<Integer> filter) {
        var allFeatures = this.orderedRenderQueue;

        for (Map.Entry<Integer, Set<InstalledFeature>> entry : allFeatures.entrySet()) {
            if (!filter.test(entry.getKey())) continue;
            Set<InstalledFeature> featuresRenderLayer = entry.getValue();
            featuresRenderLayer.forEach(feature -> feature.paint(g, worldToScreen, w, h));
        }
    }



}
