package oth.shipeditor.components.viewer.entities.weapon;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;

/**
 * @author Ontheheavens
 * @since 31.07.2023
 */
@Getter @Setter @Builder
public final class WeaponSlotOverride implements SlotData {

    private String slotID;

    private WeaponSize weaponSize;

    private WeaponType weaponType;

    private WeaponMount weaponMount;

    private Integer renderOrderMod;

    private Double arc;

    private Double angle;

    @Override
    public String getId() {
        return slotID;
    }

    @Override
    public void changeSlotID(String newId) {
        this.setSlotID(newId);
    }

    public Double getBoxedArc() {
        return arc;
    }

    @Override
    public double getArc() {
        return arc;
    }

    public void setArc(double degrees) {
        this.arc = degrees;
    }

    @Override
    public void setAngle(double degrees) {
        this.angle = degrees;
    }

    public double getAngle() {
        return angle;
    }

    public Double getBoxedAngle() {
        return angle;
    }

}
