package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.ShipData;

import java.awt.image.BufferedImage;

/**
 * Runtime representation of ship, including sprite and data.
 * Supposed to support multiple layers in one view.
 * @author Ontheheavens
 * @since 27.05.2023
 */
public class ShipLayer {

    /**
     * Runtime representation of JSON ship files.
     */
    @Getter @Setter
    private ShipData shipData;

    private LayerPainter painter;

    /**
     * Loaded instance of PNG ship sprite.
     */
    @Getter @Setter
    private BufferedImage shipSprite;

    @Getter @Setter
    private String spriteFileName = "";

    @Getter @Setter
    private String hullFileName = "";

    public ShipLayer() {
        this(null, null);
    }

    public ShipLayer(BufferedImage sprite) {
        this(sprite, null);
    }

    public ShipLayer(ShipData data) {
        this(null, data);
    }

    public ShipLayer(BufferedImage sprite, ShipData data) {
        this.shipSprite = sprite;
        this.shipData = data;
    }

}
