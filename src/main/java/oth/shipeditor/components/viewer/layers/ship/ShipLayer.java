package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.representation.ShipData;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Runtime representation of ship, including sprite and data.
 * Supposed to support multiple layers in one view.
 * @author Ontheheavens
 * @since 27.05.2023
 */
@Getter @Setter
public class ShipLayer extends ViewerLayer {

    /**
     * Runtime representation of JSON ship files.
     */
    private ShipData shipData;

    private String hullFileName = "";

    private String skinFileName = "";

    private ShipHull hull;

    private List<ShipSkin> skins;

    public ShipLayer() {}

    public ShipLayer(BufferedImage sprite) {
        this.setSprite(sprite);
    }

    @Override
    public void setPainter(LayerPainter painter) {
        if (!(painter instanceof ShipPainter)) {
            throw new IllegalArgumentException("ShipLayer provided with incompatible instance of LayerPainter!");
        }
        super.setPainter(painter);
    }

    @Override
    public ShipPainter getPainter() {
        return (ShipPainter) super.getPainter();
    }

}
