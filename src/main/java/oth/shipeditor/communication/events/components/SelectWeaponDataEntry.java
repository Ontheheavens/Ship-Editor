package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;

/**
 * @author Ontheheavens
 * @since 18.09.2023
 */
public record SelectWeaponDataEntry(WeaponCSVEntry entry) implements ComponentEvent {

}
