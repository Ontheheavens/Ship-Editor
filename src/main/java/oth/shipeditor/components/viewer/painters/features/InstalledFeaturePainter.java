package oth.shipeditor.components.viewer.painters.features;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.utility.StaticController;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@Getter @Setter
@Log4j2
public final class InstalledFeaturePainter {

    @Getter @Setter
    private static WeaponCSVEntry selectedWeaponForInstall;

    @Getter @Setter
    private static ShipCSVEntry selectedModuleForInstall;

    private PainterVisibility builtInsVisibility;

    private WeaponSlotPoint cachedSelectCounterpart;

    public InstalledFeaturePainter() {
        this.builtInsVisibility = PainterVisibility.ALWAYS_SHOWN;
    }

    private boolean checkVisibilityForBuiltIns(ShipPainter painter) {
        var visibility = this.getBuiltInsVisibility();
        boolean layerActive = painter.isLayerActive();
        if (visibility == PainterVisibility.ALWAYS_HIDDEN) return false;
        if (visibility == PainterVisibility.SHOWN_WHEN_EDITED && InstalledFeaturePainter.isInteractable(painter)) return true;
        if (visibility == PainterVisibility.SHOWN_WHEN_SELECTED && layerActive) return true;
        return visibility == PainterVisibility.ALWAYS_SHOWN;
    }

    private static boolean isInteractable(ShipPainter painter) {
        EditorInstrument editorMode = StaticController.getEditorMode();
        boolean eligibleMode = editorMode == EditorInstrument.BUILT_IN_WEAPONS
                || editorMode == EditorInstrument.DECORATIVES
                || editorMode == EditorInstrument.VARIANT;
        return eligibleMode && painter.isLayerActive();
    }

    private Map<Integer, Set<InstalledFeature>> getInstallablesToPaint(ShipPainter painter) {
        Map<Integer, Set<InstalledFeature>> result = new TreeMap<>();

        var builtIns = painter.getBuiltInWeaponsWithSkin();
        var slotPainter = painter.getWeaponSlotPainter();
        if (checkVisibilityForBuiltIns(painter)) {
            builtIns.forEach((slotID, feature) ->
                    this.prepareFeature(result, slotPainter, slotID, feature));
        }

        ShipVariant shipVariant = painter.getActiveVariant();
        if (shipVariant != null && !shipVariant.isEmpty()) {
            var modules = shipVariant.getFittedModules();
            if (modules != null) {
                modules.forEach((slotID, feature) ->
                        this.prepareFeature(result, slotPainter, slotID, feature));
            }
            var allWeapons = shipVariant.getAllFittedWeapons();
            if (allWeapons != null) {
                allWeapons.forEach((slotID, feature) ->
                        this.prepareFeature(result, slotPainter, slotID, feature));
            }
        }

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

        return result;
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

    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h, ShipPainter painter) {
        var allFeatures = this.getInstallablesToPaint(painter);

        for (Map.Entry<Integer, Set<InstalledFeature>> entry : allFeatures.entrySet()) {
            Set<InstalledFeature> featuresRenderLayer = entry.getValue();
            featuresRenderLayer.forEach(feature -> feature.paint(g, worldToScreen, w, h));
        }
    }

}
