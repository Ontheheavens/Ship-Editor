package oth.shipeditor.communication.events.viewer.layers.weapons;

import oth.shipeditor.communication.events.viewer.layers.LayerEvent;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
public record WeaponLayerCreated(WeaponLayer newLayer) implements LayerEvent {

}
