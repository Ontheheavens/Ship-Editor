package oth.shipeditor.components.layering;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.text.StringValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 02.09.2023
 */
@Getter @Setter
public class WeaponLayerTab extends LayerTab {

    private String spriteName;

    private String underSpriteName;

    private String gunSpriteName;

    private String glowSpriteName;

    private String specFileName;

    WeaponLayerTab(WeaponLayer layer) {
        super(layer);
    }

    @Override
    public String getTabTooltip() {
        String notLoaded = StringValues.NOT_LOADED;

        String mainSprite = spriteName;
        if (Objects.equals(mainSprite, "") || mainSprite == null) {
            mainSprite = notLoaded;
        }
        String mainSpriteNameLine = "Main sprite file: " + mainSprite;

        String gunSprite = gunSpriteName;
        if (Objects.equals(gunSprite, "") || gunSprite == null) {
            gunSprite = notLoaded;
        }
        String gunSpriteNameLine = "Gun sprite file: " + gunSprite;

        String underSprite = underSpriteName;
        if (Objects.equals(underSprite, "") || underSprite == null) {
            underSprite = notLoaded;
        }
        String underSpriteNameLine = "Under sprite file: " + underSprite;

        String glowSprite = glowSpriteName;
        if (Objects.equals(glowSprite, "") || glowSprite == null) {
            glowSprite = notLoaded;
        }
        String glowSpriteNameLine = "Glow sprite file: " + glowSprite;

        String specFile = specFileName;
        if (Objects.equals(specFile, "") || specFile == null) {
            specFile = notLoaded;
        }
        String specFileNameLine = "Weapon spec file: " + specFile;

        List<String> result = new ArrayList<>();
        result.add(mainSpriteNameLine);
        result.add(gunSpriteNameLine);
        result.add(underSpriteNameLine);
        result.add(glowSpriteNameLine);
        result.add(specFileNameLine);

        return Utility.getWithLinebreaks(result.toArray(new String[0]));
    }

}
