package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.ShieldPointPainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.*;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.ship.HullStyle;
import oth.shipeditor.representation.ship.SkinSpecFile;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.utility.graphics.Sprite;

import java.util.*;

/**
 * Runtime representation of ship, including sprite and data.
 * Supposed to support multiple layers in one view.
 * @author Ontheheavens
 * @since 27.05.2023
 */
@Getter
public class ShipLayer extends ViewerLayer {

    @Setter
    private String activeSkinFileName = "";

    @Setter
    private ShipHull hull;

    @Setter
    private Set<ShipSkin> skins;

    /**
     * Keys are variant IDs.
     */
    private final Map<String, ShipVariant> loadedVariants;

    private final FeaturesOverseer featuresOverseer;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public ShipLayer() {
        featuresOverseer = new FeaturesOverseer(this);
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

    public void setHullStyle(HullStyle style) {
        ShipHull shipHull = this.getHull();
        shipHull.setHullStyle(style);
        ShipPainter shipPainter = this.getPainter();
        ShieldPointPainter shieldPointPainter = shipPainter.getShieldPointPainter();
        shieldPointPainter.setShieldStyle(style);
    }

    public String getHullFileName() {
        if (hull != null) {
            return hull.getHullFileName();
        }
        return "";
    }

    public String getRelativeSpritePath() {
        ShipPainter shipPainter = getPainter();
        if (shipPainter != null) {
            Sprite baseHullSprite = shipPainter.getBaseHullSprite();
            return baseHullSprite.getPathFromPackage();
        } else return null;
    }

    public String getSpriteName() {
        ShipPainter shipPainter = getPainter();
        if (shipPainter != null) {
            Sprite baseHullSprite = shipPainter.getBaseHullSprite();
            return baseHullSprite.getFilename();
        } else return null;
    }

    @Override
    public void setPainter(LayerPainter painter) {
        if (!(painter instanceof ShipPainter)) {
            throw new IllegalArgumentException("ShipLayer provided with incompatible instance of LayerPainter!");
        }
        super.setPainter(painter);
    }

    public ShipSkin getActiveSkin() {
        ShipPainter shipPainter = this.getPainter();
        if (shipPainter == null || shipPainter.isUninitialized()) return null;
        var skin = shipPainter.getActiveSkin();
        if (skin == null || skin.isBase()) return null;
        return skin;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public int getTotalOP() {
        var activeSkin = this.getActiveSkin();
        if (activeSkin != null) {
            var skinOPs = activeSkin.getOrdnancePoints();
            if (skinOPs != null) {
                return skinOPs;
            }
        }
        // There is no way to know base hull OPs without parsing the CSV tables.
        // So, we might as well query the repository.
        if (hull == null) return -1;
        String hullID = hull.getHullID();
        var shipEntry = GameDataRepository.retrieveShipCSVEntryByID(hullID);
        if (shipEntry == null) return -1;
        return shipEntry.getBaseTotalOP();
    }

    public int getBayCount() {
        int result = 0;
        var shipHull = this.getHull();
        if (shipHull != null) {
            var gameData = SettingsManager.getGameData();
            var allShipEntries = gameData.getAllShipEntries();
            var selectedCSVEntry = allShipEntries.get(shipHull.getHullID());
            if (selectedCSVEntry != null) {
                result = selectedCSVEntry.getBayCount();
            }
        } else {
            return -1;
        }
        return result;
    }

    public int getTotalOPInWings() {
        int totalOPInWings = 0;
        ShipVariant activeVariant = getActiveVariant();
        if (activeVariant == null) {
            return -1;
        }
        return activeVariant.getTotalOPInWings();
    }

    public int getTotalUsedOP() {
        int totalUsedOP = 0;
        ShipVariant activeVariant = getActiveVariant();
        if (activeVariant == null) {
            return -1;
        }
        return activeVariant.getTotalUsedOP(this);
    }

    public int getBuiltInWingsCount() {
        int result = 0;
        ShipHull layerHull = this.getHull();
        if (layerHull == null) {
            return -1;
        }
        var wings = layerHull.getBuiltInWings();
        if (wings != null) {
            result += wings.size();
        }

        ShipPainter shipPainter = getPainter();
        ShipSkin activeSkin = shipPainter.getActiveSkin();
        if (activeSkin != null && !activeSkin.isBase()) {
            var skinWings = activeSkin.getBuiltInWings();
            result += skinWings.size();
        }
        return result;
    }

    public ShipVariant getActiveVariant() {
        ShipPainter shipPainter = this.getPainter();
        if (shipPainter == null || shipPainter.isUninitialized()) return null;
        var activeVariant = shipPainter.getActiveVariant();
        if (activeVariant == null || activeVariant.isEmpty()) return null;
        return activeVariant;
    }

    public void initializeHullData(HullSpecFile hullSpecFile) {
        ShipHull shipHull = this.getHull();
        if (shipHull == null) {
            shipHull = new ShipHull();
        }
        shipHull.initialize(hullSpecFile);
        this.hull = shipHull;
        ShipPainter shipPainter = getPainter();
        shipPainter.initFromHullSpec(hullSpecFile);

        this.loadedVariants.clear();
        this.skins.clear();
        this.skins.add(ShipSkin.EMPTY);

        shipPainter.activateEmptySkin();
        shipPainter.selectVariant(VariantFile.empty());
    }

    @Override
    public ShipPainter getPainter() {
        return (ShipPainter) super.getPainter();
    }

    public ShipSkin addSkin(SkinSpecFile skinSpecFile) {
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
