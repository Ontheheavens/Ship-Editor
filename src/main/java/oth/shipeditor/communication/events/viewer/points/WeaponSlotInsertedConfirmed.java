package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public record WeaponSlotInsertedConfirmed(WeaponSlotPoint toInsert, int precedingIndex) implements PointEvent {

}
