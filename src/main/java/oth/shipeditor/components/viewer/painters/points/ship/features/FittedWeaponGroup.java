package oth.shipeditor.components.viewer.painters.points.ship.features;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.undo.EditDispatch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public void addFitting(String slotID, InstalledFeature weapon) {
        weapons.put(slotID, weapon);
    }

    public boolean containsFitting(InstalledFeature feature) {
        return weapons.containsValue(feature);
    }

    public String getIndexToDisplay() {
        List<FittedWeaponGroup> weaponGroups = parent.getWeaponGroups();
        return "#" + (weaponGroups.indexOf(this) + 1);
    }

    public void removeBySlotID(String slotID) {
        ListOrderedMap<String, InstalledFeature> installedWeapons = this.getWeapons();
        InstalledFeature existing = installedWeapons.get(slotID);
        EditDispatch.postFeatureUninstalled(installedWeapons, slotID, existing, null);
    }

    public void uninstallAll() {
        Map<String, InstalledFeature> temporaryMap = new LinkedHashMap<>(this.weapons);
        temporaryMap.forEach((slotID, feature) -> {
            InstalledFeature existing = this.weapons.get(slotID);
            EditDispatch.postFeatureUninstalled(this.weapons, slotID, existing, null);
        });
    }

}
