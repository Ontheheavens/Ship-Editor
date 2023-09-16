package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipDataCreated;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.SkinSpecFile;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Runtime representation of ship, including sprite and data.
 * Supposed to support multiple layers in one view.
 * @author Ontheheavens
 * @since 27.05.2023
 */
public class ShipLayer extends ViewerLayer {

    /**
     * Storage of raw JSON-deserialized ship files.
     */
    @Getter
    private ShipData shipData;

    @Getter @Setter
    private String activeSkinFileName = "";

    @Getter @Setter
    private ShipHull hull;

    @Getter @Setter
    private Set<ShipSkin> skins;

    /**
     * Keys are variant IDs.
     */
    @Getter
    private final Map<String, ShipVariant> loadedVariants;

    public ShipLayer() {
        loadedVariants = new HashMap<>();
        skins = new HashSet<>();
        // Adding default, signifies base hull.
        skins.add(ShipSkin.EMPTY);
    }

    public String getShipID() {
        ShipPainter shipPainter = getPainter();
        ShipSkin activeSkin = shipPainter.getActiveSkin();
        if (activeSkin != null && !activeSkin.isBase()) {
            return activeSkin.getSkinHullId();
        }
        return hull.getHullID();
    }

    public String getHullFileName() {
        if (shipData != null) {
            return shipData.getHullFileName();
        }
        return "";
    }

    @Override
    public void setPainter(LayerPainter painter) {
        if (!(painter instanceof ShipPainter)) {
            throw new IllegalArgumentException("ShipLayer provided with incompatible instance of LayerPainter!");
        }
        super.setPainter(painter);
    }

    public void createShipData(HullSpecFile hullSpecFile) {
        ShipData data = this.getShipData();
        if (data != null ) {
            data.setHullSpecFile(hullSpecFile);
        } else {
            this.shipData = new ShipData(hullSpecFile);
            this.hull = new ShipHull();
        }
        EventBus.publish(new ShipDataCreated(this));
    }

    @Override
    public ShipPainter getPainter() {
        return (ShipPainter) super.getPainter();
    }

    public ShipSkin addSkin(SkinSpecFile skinSpecFile) {
        Map<String, SkinSpecFile> shipDataSkins = this.shipData.getSkins();
        shipDataSkins.put(skinSpecFile.getSkinHullId(), skinSpecFile);
        ShipSkin skin = ShipSkin.createFromSpec(skinSpecFile);
        this.skins.add(skin);
        return skin;
    }

    public List<String> getSkinFileNames() {
        Set<ShipSkin> shipSkins = this.getSkins();
        List<String> result = new ArrayList<>();
        shipSkins.forEach(skin -> {
            if (skin.isBase()) return;
            result.add(skin.getFileName());
        });
        return result;
    }

    // This getter business is not good at all.
    // However, given the constraints, best to let it be and move on to finish the project.

    public Map<String, InstalledFeature> getBuiltInsFromBaseHull() {
        var painter = this.getPainter();
        Supplier<Map<String, InstalledFeature>> getter = painter::getBuiltInWeapons;
        return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
            String slotID = featureEntry.getKey();
            return slotPainter.getSlotByID(slotID) != null && !slotPainter.isSlotDecorative(slotID);
        });
    }

    public Map<String, InstalledFeature> getDecorativesFromBaseHull() {
        var painter = this.getPainter();
        Supplier<Map<String, InstalledFeature>> getter = painter::getBuiltInWeapons;
        return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
            String slotID = featureEntry.getKey();
            return slotPainter.isSlotDecorative(slotID);
        });
    }

    public List<String> getBuiltInsRemovedBySkin() {
        return getFilteredRemovables((slotPainter, slotID) ->
                slotPainter.getSlotByID(slotID) != null
                        && !slotPainter.isSlotDecorative(slotID));
    }

    public List<String> getDecorativesRemovedBySkin() {
        return getFilteredRemovables(WeaponSlotPainter::isSlotDecorative);
    }

    public Map<String, InstalledFeature> getBuiltInsFromSkin() {
        var painter = this.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            Supplier<Map<String, InstalledFeature>> getter = activeSkin::getInitializedBuiltIns;
            return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
                String slotID = featureEntry.getKey();
                return slotPainter.getSlotByID(slotID) != null && !slotPainter.isSlotDecorative(slotID);
            });
        }
        return null;
    }

    public Map<String, InstalledFeature> getDecorativesFromSkin() {
        var painter = this.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            Supplier<Map<String, InstalledFeature>> getter = activeSkin::getInitializedBuiltIns;
            return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
                String slotID = featureEntry.getKey();
                return slotPainter.isSlotDecorative(slotID);
            });
        }
        return null;
    }

    private List<String> getFilteredRemovables(BiFunction<WeaponSlotPainter, String, Boolean> filter) {
        ShipPainter painter = this.getPainter();
        if (painter == null) return null;
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            var removed = activeSkin.getRemoveBuiltInWeapons();
            if (removed == null || removed.isEmpty()) return null;

            var slotPainter = painter.getWeaponSlotPainter();
            List<String> result = new ArrayList<>(removed.size());
            removed.forEach(slotID -> {
                if (filter.apply(slotPainter, slotID)) {
                    result.add(slotID);
                }
            });
            return result;
        }
        return null;
    }

    /**
     * Should be used only for UI view purposes, not intended for add/remove - use original collection for that.
     * @return a new Map with filtered entries from original getter.
     */
    private Map<String, InstalledFeature> getFilteredInstallables(
            Supplier<Map<String, InstalledFeature>> getter,
            BiFunction<WeaponSlotPainter, Map.Entry<String, InstalledFeature>, Boolean> filter) {
        var painter = this.getPainter();
        if (painter == null) return null;
        var installedFeatureMap = getter.get();
        if (installedFeatureMap == null || installedFeatureMap.isEmpty()) return null;
        var slotPainter = painter.getWeaponSlotPainter();
        if (slotPainter == null) return null;

        Map<String, InstalledFeature> result = new LinkedHashMap<>();
        Set<Map.Entry<String, InstalledFeature>> entries = installedFeatureMap.entrySet();
        Stream<Map.Entry<String, InstalledFeature>> stream = entries.stream();
        stream.forEach(featureEntry -> {
            if (filter.apply(slotPainter, featureEntry)) {
                result.put(featureEntry.getKey(), featureEntry.getValue());
            }
        });
        return result;
    }

}
