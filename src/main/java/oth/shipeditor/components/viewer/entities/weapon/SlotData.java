package oth.shipeditor.components.viewer.entities.weapon;

import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;

/**
 * @author Ontheheavens
 * @since 16.08.2023
 */
public interface SlotData {

    String getId();

    void changeSlotID(String newId);

    WeaponType getWeaponType();

    void setWeaponType(WeaponType newType);

    WeaponMount getWeaponMount();

    void setWeaponMount(WeaponMount newMount);

    WeaponSize getWeaponSize();

    void setWeaponSize(WeaponSize newSize);

    double getArc();

    void setArc(double degrees);

    double getAngle();

    void setAngle(double degrees);

}
