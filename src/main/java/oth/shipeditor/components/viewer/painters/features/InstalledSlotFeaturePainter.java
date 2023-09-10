package oth.shipeditor.components.viewer.painters.features;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.PainterVisibility;

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
public final class InstalledSlotFeaturePainter {

    // TODO: class still under construction, will need a cleanup!.

    private static PainterVisibility builtInsVisibility;

    private static PainterVisibility modulesVisibility;

    private static PainterVisibility fittedWeaponsVisibility;

    @SuppressWarnings("StaticCollection")
    private static Map<EditorInstrument, PainterVisibility> installableKindVisibility;

    private InstalledSlotFeaturePainter() {
    }

//    static boolean checkVisibilityForInstallableKind(ShipPainter painter) {
//        PainterVisibility visibility = getVisibilityMode();
//        if (visibility == PainterVisibility.ALWAYS_HIDDEN) return false;
//        if (visibility == PainterVisibility.SHOWN_WHEN_EDITED && this.isInteractionEnabled()) return true;
//        if (visibility == PainterVisibility.SHOWN_WHEN_SELECTED && painter.isLayerActive()) return true;
//        return visibility == PainterVisibility.ALWAYS_SHOWN;
//    }

    @SuppressWarnings("CodeBlock2Expr")
    private static Map<Integer, Set<InstalledFeature>> getInstallablesToPaint(ShipPainter painter) {
        Map<Integer, Set<InstalledFeature>> result = new TreeMap<>();

        // TODO: implement visibility for installable feature kinds.

        var builtIns = painter.getBuiltInWeaponsWithSkin();
        if (true) {
            builtIns.forEach((slotID, feature) -> {
                InstalledSlotFeaturePainter.prepareInstalledForPainting(result, painter, slotID, feature);
            });
        }

        ShipVariant shipVariant = painter.getActiveVariant();
        if (shipVariant != null && !shipVariant.isEmpty()) {
            var modules = shipVariant.getFittedModules();
            if (modules != null && true) {
                modules.forEach((slotID, feature) -> {
                    InstalledSlotFeaturePainter.prepareInstalledForPainting(result, painter, slotID, feature);
                });
            }
            var allWeapons = shipVariant.getAllFittedWeapons();
            if (allWeapons != null && true) {
                allWeapons.forEach((slotID, feature) -> {
                    InstalledSlotFeaturePainter.prepareInstalledForPainting(result, painter, slotID, feature);
                });
            }
        }
        return result;
    }

    private static void prepareInstalledForPainting(Map<Integer, Set<InstalledFeature>> collection,
                                                          ShipPainter painter, String slotID,
                                                          InstalledFeature feature) {
        int renderOrder = InstalledSlotFeaturePainter.refreshSlotData(painter, slotID, feature);
        var renderLayer = collection.computeIfAbsent(renderOrder,
                k -> new LinkedHashSet<>());
        renderLayer.add(feature);
        collection.put(renderOrder, renderLayer);
    }

    private static int refreshSlotData(ShipPainter painter, String slotID, InstalledFeature feature) {
        var slotPainter = painter.getWeaponSlotPainter();
        WeaponSlotPoint slotPoint = slotPainter.getSlotByID(slotID);

        int renderOrder = feature.computeRenderOrder(slotPoint);
        feature.refreshPaintCircumstance(slotPoint);
        return renderOrder;
    }

    public static void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h, ShipPainter painter) {
        var allFeatures = InstalledSlotFeaturePainter.getInstallablesToPaint(painter);

        for (Map.Entry<Integer, Set<InstalledFeature>> entry : allFeatures.entrySet()) {
            Set<InstalledFeature> featuresRenderLayer = entry.getValue();
            featuresRenderLayer.forEach(feature -> feature.paint(g, worldToScreen, w, h));
        }
    }

}
