package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public record SlotPointsSorted(List<WeaponSlotPoint> rearranged) implements PointEvent {

}
