package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SkinFileOpened;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
@Getter
public class ShipCSVEntry {

    private final Map<String, String> rowData;

    private final Hull hullFile;

    /**
     * Keys are simple names of skin files, e.g.: legion_xiv.skin.
     */
    private final Map<String, Skin> skins;

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
            this.skins.put(StringValues.DEFAULT, activeSkin);
        }
    }

    public void setActiveSkin(Skin input) {
        if (!skins.containsValue(input)) {
            throw new RuntimeException("Attempt to set incompatible skin on ship entry!");
        }
        this.activeSkin = input;
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

        Path spriteFilePath = Path.of(spriteName);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, packagePath);

        FileUtilities.createShipLayerWithSprite(spriteFile);
        EventBus.publish(new HullFileOpened(this.hullFile, this.getHullFileName()));
        if (skinChosen) {
            String skinFileName = this.activeSkin.getSkinFilePath().getFileName().toString();
            EventBus.publish(new SkinFileOpened(this.activeSkin, skinFileName));
        }
    }

    public List<String> getBuiltInHullmods() {
        String[] fromHull = hullFile.getBuiltInMods();
        List<String> hullmodIDs = new ArrayList<>();
        if (fromHull != null) {
            hullmodIDs.addAll(List.of(fromHull));
        }
        Skin skin = this.activeSkin;
        if (skin != null && !skin.isBase()) {
            hullmodIDs.addAll(skin.getBuiltInMods());
        }
        return hullmodIDs;
    }

}
