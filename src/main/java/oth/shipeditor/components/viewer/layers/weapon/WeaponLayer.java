package oth.shipeditor.components.viewer.layers.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.WeaponSpecFile;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
public class WeaponLayer extends ViewerLayer {

    @Getter @Setter
    private WeaponSpecFile specFile;

    @Override
    public WeaponPainter getPainter() {
        return (WeaponPainter) super.getPainter();
    }

    public String getSpecFileName() {
        return String.valueOf(specFile.getWeaponSpecFilePath().getFileName());
    }

    public String getWeaponName() {
        if (specFile != null) {
            String weaponID = specFile.getId();
            GameDataRepository repository = SettingsManager.getGameData();
            var allWeapons = repository.getAllWeaponEntries();
            if (allWeapons == null) return "";
            var weaponEntry = allWeapons.get(weaponID);
            if (weaponEntry != null) {
                return weaponEntry.toString();
            }
        }
        return "";
    }

}
