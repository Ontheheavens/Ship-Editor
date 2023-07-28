package oth.shipeditor.components.viewer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.text.StringConstants;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
public class WeaponSlotPoint extends BaseWorldPoint {

    private String id;

    private WeaponSize weaponSize;

    private WeaponType weaponType;

    private WeaponMount weaponMount;

    private double renderOrderMod;

    private double arc;

    private double angle;

}
