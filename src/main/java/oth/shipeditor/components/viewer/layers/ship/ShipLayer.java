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

}
