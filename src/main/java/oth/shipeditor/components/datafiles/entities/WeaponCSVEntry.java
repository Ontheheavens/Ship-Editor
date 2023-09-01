package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@Getter
public class WeaponCSVEntry implements CSVEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String weaponID;

    @Setter
    private WeaponSpecFile specFile;

    public WeaponCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        weaponID = this.rowData.get("id");
    }

    @Override
    public String getID() {
        return weaponID;
    }


    public void loadLayerFromEntry() {
        String spriteName = this.specFile.getTurretSprite();

        Path spriteFilePath = Path.of(spriteName);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, this.packageFolderPath);

        WeaponLayer newLayer = FileUtilities.createWeaponLayerWithSprite(spriteFile);

        var painter = newLayer.getPainter();

        String turretGunSprite = specFile.getTurretGunSprite();
        setSpecSpriteFromPath(turretGunSprite, painter::setTurretGunSprite);
        String turretGlowSprite = specFile.getTurretGlowSprite();
        setSpecSpriteFromPath(turretGlowSprite, painter::setTurretGlowSprite);
        String turretUnderSprite = specFile.getTurretUnderSprite();
        setSpecSpriteFromPath(turretUnderSprite, painter::setTurretUnderSprite);

        String hardpointSprite = specFile.getHardpointSprite();
        setSpecSpriteFromPath(hardpointSprite, painter::setHardpointSprite);
        String hardpointGunSprite = specFile.getHardpointGunSprite();
        setSpecSpriteFromPath(hardpointGunSprite, painter::setHardpointGunSprite);
        String hardpointGlowSprite = specFile.getHardpointGlowSprite();
        setSpecSpriteFromPath(hardpointGlowSprite, painter::setHardpointGlowSprite);
        String hardpointUnderSprite = specFile.getHardpointUnderSprite();
        setSpecSpriteFromPath(hardpointUnderSprite, painter::setHardpointUnderSprite);

    }

    private void setSpecSpriteFromPath(String pathInPackage, Consumer<Sprite> setter) {
        if (pathInPackage != null && !pathInPackage.isEmpty()) {
            File spriteFile = FileLoading.fetchDataFile(Path.of(pathInPackage), this.packageFolderPath);

            Sprite newSprite = FileLoading.loadSprite(spriteFile);
            setter.accept(newSprite);
        }
    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = StringValues.UNTITLED;
        }
        return displayedName;
    }

}
