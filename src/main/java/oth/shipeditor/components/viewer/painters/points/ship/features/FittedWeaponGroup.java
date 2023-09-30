package oth.shipeditor.components.viewer.painters.points.ship.features;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 09.09.2023
 */
@Getter
public class FittedWeaponGroup {

    @Setter
    private boolean autofire;

    @Setter
    private FireMode mode;

    private final ListOrderedMap<String, InstalledFeature> weapons;

    private final ShipVariant parent;

    @SuppressWarnings("BooleanParameter")
    public FittedWeaponGroup(ShipVariant variant, boolean autofireInput, FireMode modeInput) {
        this.parent = variant;
        this.autofire = autofireInput;
        this.mode = modeInput;
        this.weapons = new ListOrderedMap<>();
    }

    public String getIndexToDisplay() {
        List<FittedWeaponGroup> weaponGroups = parent.getWeaponGroups();
        return "#" + (weaponGroups.indexOf(this) + 1);
    }

}
