package oth.shipeditor.components.viewer.layers.weapon;

import oth.shipeditor.components.viewer.layers.ViewerLayer;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
public class WeaponLayer extends ViewerLayer {

    @Override
    public WeaponPainter getPainter() {
        return (WeaponPainter) super.getPainter();
    }

}
