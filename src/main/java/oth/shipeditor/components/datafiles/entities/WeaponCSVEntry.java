package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;
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
        String turretSprite = this.specFile.getTurretSprite();

        Path spriteFilePath = Path.of(turretSprite);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, this.packageFolderPath);

        Sprite sprite = FileLoading.loadSprite(spriteFile);

        var manager = StaticController.getLayerManager();
        WeaponLayer newLayer = manager.createWeaponLayer();
        newLayer.setSpecFile(specFile);

        WeaponPainter weaponPainter = createPainterFromEntry(newLayer, specFile);
        newLayer.setPainter(weaponPainter);

        manager.setActiveLayer(newLayer);
        var viewer = StaticController.getViewer();
        viewer.loadLayer(newLayer, sprite);

        EventBus.publish(new ActiveLayerUpdated(newLayer));
    }

    /**
     * @param layer can be null.
     */
    @SuppressWarnings("WeakerAccess")
    public WeaponPainter createPainterFromEntry(WeaponLayer layer, WeaponSpecFile weaponSpecFile) {
        WeaponPainter weaponPainter = new WeaponPainter(layer);
        WeaponSprites spriteHolder = weaponPainter.getWeaponSprites();

        String turretSprite = weaponSpecFile.getTurretSprite();
        setSpecSpriteFromPath(turretSprite, spriteHolder::setTurretSprite);
        String turretGunSprite = weaponSpecFile.getTurretGunSprite();
        setSpecSpriteFromPath(turretGunSprite, spriteHolder::setTurretGunSprite);
        String turretGlowSprite = weaponSpecFile.getTurretGlowSprite();
        setSpecSpriteFromPath(turretGlowSprite, spriteHolder::setTurretGlowSprite);
        String turretUnderSprite = weaponSpecFile.getTurretUnderSprite();
        setSpecSpriteFromPath(turretUnderSprite, spriteHolder::setTurretUnderSprite);

        String hardpointSprite = weaponSpecFile.getHardpointSprite();
        setSpecSpriteFromPath(hardpointSprite, spriteHolder::setHardpointSprite);
        String hardpointGunSprite = weaponSpecFile.getHardpointGunSprite();
        setSpecSpriteFromPath(hardpointGunSprite, spriteHolder::setHardpointGunSprite);
        String hardpointGlowSprite = weaponSpecFile.getHardpointGlowSprite();
        setSpecSpriteFromPath(hardpointGlowSprite, spriteHolder::setHardpointGlowSprite);
        String hardpointUnderSprite = weaponSpecFile.getHardpointUnderSprite();
        setSpecSpriteFromPath(hardpointUnderSprite, spriteHolder::setHardpointUnderSprite);

        Sprite loadedTurretSprite = spriteHolder.getTurretSprite();
        weaponPainter.setSprite(loadedTurretSprite.image());
        return weaponPainter;
    }


    private void setSpecSpriteFromPath(String pathInPackage, Consumer<Sprite> setter) {
        Utility.setSpriteFromPath(pathInPackage, setter, this.packageFolderPath);
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
