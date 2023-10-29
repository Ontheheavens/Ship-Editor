package oth.shipeditor.components.layering;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Empty marker component, only serves to track tabs and their layers.
 */
@Getter
final class ShipLayerTab extends LayerTab {

    @Setter
    private String spriteFileName;

    @Setter
    private String hullFileName;

    @Setter
    private List<String> skinFileNames;

    @Setter
    private String activeSkinFileName;

    ShipLayerTab(ShipLayer layer) {
        super(layer);
        ShipPainter shipPainter = layer.getPainter();
        if (shipPainter != null) {
            Sprite sprite = shipPainter.getSprite();
            this.spriteFileName = sprite.getFilename();
        } else {
            this.spriteFileName = StringValues.NOT_LOADED;
        }

        this.hullFileName = layer.getHullFileName();
        this.skinFileNames = layer.getSkinFileNames();
    }

    /**
     * @return HTML-formatted string that enables multi-line tooltip setup.
     */
    public String getTabTooltip() {
        String notLoaded = StringValues.NOT_LOADED;
        String sprite = spriteFileName;
        if (Objects.equals(sprite, "")) {
            sprite = notLoaded;
        }
        String spriteNameLine = StringValues.SPRITE_FILE + sprite;

        String hull = hullFileName;
        if (Objects.equals(hull, "")) {
            hull = notLoaded;
        }
        String hullNameLine = "Hull file: " + hull;

        Collection<String> skinNameLines = new ArrayList<>();
        skinFileNames.forEach(s -> {
            String skin = s;
            if (Objects.equals(skin, "")) {
                skin = StringValues.NOT_LOADED;
            }
            else if (skin.equals(activeSkinFileName)) {
                skin = "<font color=blue>" + skin + "</font>";
            }
            String skinNameLine = "Skin file: " + skin;
            skinNameLines.add(skinNameLine);
        });
        if (skinNameLines.isEmpty()) {
            skinNameLines.add("Skin file: Not loaded");
        }

        List<String> result = new ArrayList<>();
        result.add(spriteNameLine);
        result.add(hullNameLine);
        result.addAll(skinNameLines);

        return Utility.getWithLinebreaks(result.toArray(new String[0]));
    }

}
