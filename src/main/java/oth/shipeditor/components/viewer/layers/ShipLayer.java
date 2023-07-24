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
@Getter @Setter
public class ShipLayer {

    /**
     * Runtime representation of JSON ship files.
     */
    private ShipData shipData;

    /**
     * Loaded instance of PNG ship sprite.
     */
    private BufferedImage shipSprite;

    private String spriteFileName = "";

    private String hullFileName = "";

    private String skinFileName = "";

    private LayerPainter painter;

    public ShipLayer() {
        this(null, null);
    }

    public ShipLayer(BufferedImage sprite) {
        this(sprite, null);
    }

    public ShipLayer(BufferedImage sprite, ShipData data) {
        this.shipSprite = sprite;
        this.shipData = data;
    }

    @Override
    public String toString() {
        if (!spriteFileName.isEmpty()) {
            return spriteFileName;
        }
        return super.toString();
    }

}
