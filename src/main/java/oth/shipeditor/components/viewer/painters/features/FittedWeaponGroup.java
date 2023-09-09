package oth.shipeditor.components.viewer.painters.features;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
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

    private final Map<String, InstalledFeature> weapons;

    @SuppressWarnings("BooleanParameter")
    public FittedWeaponGroup(boolean autofireInput, FireMode modeInput) {
        this.autofire = autofireInput;
        this.mode = modeInput;
        this.weapons = new LinkedHashMap<>();
    }

}
