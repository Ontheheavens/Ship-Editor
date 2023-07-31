package oth.shipeditor.components.viewer.entities.weapon;

import lombok.Getter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;

/**
 * @author Ontheheavens
 * @since 31.07.2023
 */
@Getter
public final class WeaponSlotOverride {

    private final String slotID;

    private final WeaponSize weaponSize;

    private final WeaponType weaponType;

    private final WeaponMount weaponMount;

    private final Integer renderOrderMod;

    private final Double arc;

    private final Double angle;

    private WeaponSlotOverride(Builder builder) {
        this.weaponSize = builder.weaponSize;
        this.weaponType = builder.weaponType;
        this.weaponMount = builder.weaponMount;
        this.renderOrderMod = builder.renderOrderMod;
        this.arc = builder.arc;
        this.angle = builder.angle;
        this.slotID = builder.slotID;
    }

    @SuppressWarnings({"PublicInnerClass", "unused"})
    public static class Builder {

        private String slotID;
        private WeaponSize weaponSize;
        private WeaponType weaponType;
        private WeaponMount weaponMount;
        private Integer renderOrderMod;
        private Double arc;
        private Double angle;

        public static Builder override() {
            return new Builder();
        }

        public Builder withWeaponSize(WeaponSize inputSize) {
            this.weaponSize = inputSize;
            return this;
        }

        public Builder withWeaponType(WeaponType inputType) {
            this.weaponType = inputType;
            return this;
        }

        public Builder withWeaponMount(WeaponMount inputMount) {
            this.weaponMount = inputMount;
            return this;
        }

        public Builder withRenderOrderMod(Integer inputOrderMod) {
            this.renderOrderMod = inputOrderMod;
            return this;
        }

        public Builder withArc(Double inputArc) {
            this.arc = inputArc;
            return this;
        }

        public Builder withAngle(Double inputAngle) {
            this.angle = inputAngle;
            return this;
        }

        public Builder withSlotID(String id) {
            this.slotID = id;
            return this;
        }

        public WeaponSlotOverride build() {
            return new WeaponSlotOverride(this);
        }
    }

}
