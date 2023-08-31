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
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.SkinSpecFile;

import java.util.*;

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
    private String hullFileName = "";

    @Getter @Setter
    private String skinFileName = "";

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
        String fileName = String.valueOf(hullSpecFile.getFilePath().getFileName());
        this.setHullFileName(fileName);
        EventBus.publish(new ShipDataCreated(this));
    }

    @Override
    public ShipPainter getPainter() {
        return (ShipPainter) super.getPainter();
    }

    public ShipSkin addSkin(SkinSpecFile skinSpecFile) {
        Map<String, SkinSpecFile> shipDataSkins = this.shipData.getSkins();
        shipDataSkins.put(skinSpecFile.getSkinHullId(), skinSpecFile);

        // This is obviously an anti-pattern, but I'm just so burned out at this point that I don't really care.
        ShipSkin skinInstance = new ShipSkin.Builder()
                .withSkinFilePath(skinSpecFile.getFilePath())
                .withContainingPackage(skinSpecFile.getContainingPackage())
                .withLoadedSkinSprite(skinSpecFile.getLoadedSkinSprite())
                .withBaseHullId(skinSpecFile.getBaseHullId())
                .withSkinHullId(skinSpecFile.getSkinHullId())
                .withShipSystem(skinSpecFile.getSystemId())
                .withHullName(skinSpecFile.getHullName())
                .withHullDesignation(skinSpecFile.getHullDesignation())
                .withHullStyle(skinSpecFile.getHullStyle())
                .withRestoreToBaseHull(skinSpecFile.isRestoreToBaseHull())
                .withIncompatibleWithBaseHull(skinSpecFile.isIncompatibleWithBaseHull())
                .withFleetPoints(skinSpecFile.getFleetPoints())
                .withOrdnancePoints(skinSpecFile.getOrdnancePoints())
                .withBaseValue(skinSpecFile.getBaseValue())
                .withSuppliesPerMonth(skinSpecFile.getSuppliesPerMonth())
                .withSuppliesToRecover(skinSpecFile.getSuppliesToRecover())
                .withDescriptionId(skinSpecFile.getDescriptionId())
                .withDescriptionPrefix(skinSpecFile.getDescriptionPrefix())
                .withCoversColor(skinSpecFile.getCoversColor())
                .withTags(skinSpecFile.getTags())
                .withTech(skinSpecFile.getTech())
                .withBuiltInWings(skinSpecFile.getBuiltInWings())
                .withFighterBays(skinSpecFile.getFighterBays())
                .withSpriteName(skinSpecFile.getSpriteName())
                .withBaseValueMult(skinSpecFile.getBaseValueMult())
                .withRemoveHints(skinSpecFile.getRemoveHints())
                .withAddHints(skinSpecFile.getAddHints())
                .withRemoveWeaponSlots(skinSpecFile.getRemoveWeaponSlots())
                .withRemoveEngineSlots(skinSpecFile.getRemoveEngineSlots())
                .withRemoveBuiltInMods(skinSpecFile.getRemoveBuiltInMods())
                .withRemoveBuiltInWeapons(skinSpecFile.getRemoveBuiltInWeapons())
                .withBuiltInMods(skinSpecFile.getBuiltInMods())
                .withBuiltInWeapons(skinSpecFile.getBuiltInWeapons())
                .withWeaponSlotChanges(skinSpecFile.getWeaponSlotChanges())
                .withEngineSlotChanges(skinSpecFile.getEngineSlotChanges())
                .build();
        this.skins.add(skinInstance);
        return skinInstance;
    }

}
