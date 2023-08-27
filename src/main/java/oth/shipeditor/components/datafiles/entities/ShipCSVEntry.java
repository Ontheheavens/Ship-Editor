package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SkinFileOpened;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
@Getter
public class ShipCSVEntry implements CSVEntry {

    private final Map<String, String> rowData;

    private final HullSpecFile hullSpecFile;

    /**
     * Keys are simple names of skin files, e.g.: legion_xiv.skin.
     */
    private final Map<String, SkinSpecFile> skins;

    private SkinSpecFile activeSkinSpecFile;

    private final String hullFileName;

    private final String hullID;

    private final Path packageFolderPath;

    public ShipCSVEntry(Map<String, String> row, Map.Entry<HullSpecFile, Map<String, SkinSpecFile>> hullWithSkins,
                        Path folder, String fileName) {
        this.packageFolderPath = folder;
        this.hullSpecFile = hullWithSkins.getKey();
        this.skins = hullWithSkins.getValue();
        this.rowData = row;
        this.hullID = row.get(StringConstants.ID);
        this.hullFileName = fileName;
        this.activeSkinSpecFile = SkinSpecFile.empty();
        if (this.skins != null) {
            this.skins.put(SkinSpecFile.DEFAULT, activeSkinSpecFile);
        }
    }

    @Override
    public String getID() {
        return hullID;
    }

    @Override
    public Path getTableFilePath() {
        return hullSpecFile.getTableFilePath();
    }

    public void setActiveSkinSpecFile(SkinSpecFile input) {
        if (!skins.containsValue(input)) {
            throw new RuntimeException("Attempt to set incompatible skin on ship entry!");
        }
        this.activeSkinSpecFile = input;
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
        String spriteName = this.hullSpecFile.getSpriteName();

        Path spriteFilePath = Path.of(spriteName);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, this.packageFolderPath);

        FileUtilities.createShipLayerWithSprite(spriteFile);
        EventBus.publish(new HullFileOpened(this.hullSpecFile, this.getHullFileName()));

        if (skins == null || skins.isEmpty()) return;

        Map<String, SkinSpecFile> eligibleSkins = new HashMap<>(skins);
        eligibleSkins.remove(SkinSpecFile.DEFAULT);
        if (eligibleSkins.isEmpty()) return;
        for (SkinSpecFile skinSpecFile : eligibleSkins.values()) {
            if (skinSpecFile == null || skinSpecFile.isBase()) continue;

            String skinSpriteName = skinSpecFile.getSpriteName();
            Path skinPackagePath = skinSpecFile.getContainingPackage();

            if (skinSpriteName == null || skinSpriteName.isEmpty()) {
                skinSpriteName = this.hullSpecFile.getSpriteName();
            }

            Path skinSpriteFilePath = Path.of(skinSpriteName);
            File skinSpriteFile = FileLoading.fetchDataFile(skinSpriteFilePath, skinPackagePath);

            Sprite skinSprite = FileLoading.loadSprite(skinSpriteFile);

            skinSpecFile.setLoadedSkinSprite(skinSprite);
            EventBus.publish(new SkinFileOpened(skinSpecFile, skinSpecFile == this.activeSkinSpecFile));
        }
    }

    public List<String> getBuiltInHullmods() {
        String[] fromHull = hullSpecFile.getBuiltInMods();
        List<String> hullmodIDs = new ArrayList<>();
        if (fromHull != null) {
            hullmodIDs.addAll(List.of(fromHull));
        }
        SkinSpecFile skinSpecFile = this.activeSkinSpecFile;
        if (skinSpecFile != null && !skinSpecFile.isBase()) {
            List<String> builtInMods = skinSpecFile.getBuiltInMods();
            if (builtInMods != null) {
                hullmodIDs.addAll(builtInMods);
            }
        }
        return hullmodIDs;
    }

}
