package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SkinFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.LayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerCyclingQueued;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Getter
public class ShipCSVEntry {

    private final Map<String, String> rowData;

    private final Hull hullFile;

    private final Map<String, Skin> skins;

    @Setter
    private Skin activeSkin;

    private final String hullFileName;

    private final String hullID;

    private final Path packageFolder;

    public ShipCSVEntry(Map<String, String> row, Map.Entry<Hull, Map<String, Skin>> hullWithSkins,
                        Path folder, String fileName) {
        this.packageFolder = folder;
        this.hullFile = hullWithSkins.getKey();
        this.skins = hullWithSkins.getValue();
        this.rowData = row;
        this.hullID = row.get(StringConstants.ID);
        this.hullFileName = fileName;
        this.activeSkin = Skin.empty();
        if (this.skins != null) {
            this.skins.put(StringConstants.DEFAULT, activeSkin);
        }
    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = rowData.get(StringConstants.DESIGNATION);
        }
        return displayedName;
    }

    public void loadLayerFromEntry() {
        Path packagePath = this.packageFolder;
        String spriteName = this.hullFile.getSpriteName();
        boolean skinChosen = !this.activeSkin.isBase();
        if (skinChosen) {
            spriteName = this.activeSkin.getSpriteName();
            packagePath = this.activeSkin.getContainingPackage();
        }
        if (spriteName == null || spriteName.isEmpty()) {
            spriteName = this.hullFile.getSpriteName();
        }

        Path spriteFilePath = packagePath.resolve(spriteName);
        File spriteFile = spriteFilePath.toFile();

        EventBus.publish(new LayerCreationQueued());
        EventBus.publish(new LayerCyclingQueued());
        BufferedImage sprite = FileUtilities.loadSprite(spriteFile);
        EventBus.publish(new SpriteOpened(sprite, spriteFile.getName()));
        EventBus.publish(new HullFileOpened(this.hullFile, this.getHullFileName()));
        if (skinChosen) {
            String skinFileName = Utility.getSkinFileName(this, this.activeSkin);
            EventBus.publish(new SkinFileOpened(this.activeSkin, skinFileName));
        }
    }

}
