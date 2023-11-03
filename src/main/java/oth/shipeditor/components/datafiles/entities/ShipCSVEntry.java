package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.SkinFileOpened;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.HullSize;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.ShipTypeHints;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
@Getter
public class ShipCSVEntry implements CSVEntry, InstallableEntry {

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

    @SuppressWarnings("WeakerAccess")
    public List<ShipTypeHints> getBaseHullHints() {
        List<ShipTypeHints> result = new ArrayList<>();
        String cellData = rowData.get(StringConstants.HINTS);
        if (cellData != null && !cellData.isEmpty()) {
            Iterable<String> hintsText = new ArrayList<>(Arrays.asList(Utility.SPLIT_BY_COMMA.split(cellData)));
            hintsText.forEach(hintText -> result.add(ShipTypeHints.valueOf(hintText)));
        }
        return result;
    }

    public HullSize getSize() {
        HullSpecFile specFile = this.getHullSpecFile();
        String hullSize = specFile.getHullSize();
        return HullSize.valueOf(hullSize);
    }

    public String getShipID() {
        if (activeSkinSpecFile != null && !activeSkinSpecFile.isBase()) {
            return activeSkinSpecFile.getSkinHullId();
        }
        return getHullID();
    }

    public String getShipName() {
        if (activeSkinSpecFile != null && !activeSkinSpecFile.isBase()) {
            return activeSkinSpecFile.getHullName();
        }
        return rowData.get(StringConstants.NAME);
    }

    public int getTotalOPWithSkin() {
        if (activeSkinSpecFile != null && !activeSkinSpecFile.isBase()) {
            return activeSkinSpecFile.getOrdnancePoints();
        }
        String ordnancePoints = rowData.get(StringConstants.ORDNANCE_POINTS_SPACED);
        return Integer.parseInt(ordnancePoints);
    }

    public int getBaseTotalOP() {
        String ordnancePoints = rowData.get(StringConstants.ORDNANCE_POINTS_SPACED);
        if (ordnancePoints == null || ordnancePoints.isEmpty()) return -1;
        int result;
        try {
            result = Integer.parseInt(ordnancePoints);
        } catch (NumberFormatException exception) {
            return -1;
        }
        return result;
    }

    public int getBayCount() {
        int result = 0;
        var entryRowData = this.getRowData();
        String fighterBays = entryRowData.get("fighter bays");

        if (fighterBays != null && !fighterBays.isEmpty()) {
            result = Integer.parseInt(fighterBays);
        }
        return result;
    }

    public String getShipSpriteName() {
        if (activeSkinSpecFile != null && !activeSkinSpecFile.isBase()) {
            String skinSpecFileSpriteName = activeSkinSpecFile.getSpriteName();
            if (skinSpecFileSpriteName != null && !skinSpecFileSpriteName.isEmpty()) {
                return skinSpecFileSpriteName;
            }
        }
        return hullSpecFile.getSpriteName();
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

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public void loadLayerFromEntry() {
        String spriteName = this.hullSpecFile.getSpriteName();
        Path spriteFilePath = Path.of(spriteName);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, this.packageFolderPath);

        if (spriteFile == null) {
            log.error("Sprite file for ship not found: {}", spriteFilePath.toString());
            JOptionPane.showMessageDialog(null,
                    "Sprite file for ship not found, layer not created: " + spriteFilePath,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ShipLayer newLayer = FileUtilities.createShipLayerWithSprite(spriteFile);
        newLayer.initializeHullData(this.hullSpecFile);

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

    /**
     * @param layer can be null.
     */
    public ShipPainter createPainterFromEntry(ShipLayer layer) {
        ShipPainter shipPainter = new ShipPainter(layer);

        String spriteName = this.hullSpecFile.getSpriteName();
        Path spriteFilePath = Path.of(spriteName);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, this.packageFolderPath);
        Sprite sprite = FileLoading.loadSprite(spriteFile);
        shipPainter.setSprite(sprite);
        shipPainter.setBaseHullSprite(sprite);

        shipPainter.initFromHullSpec(this.getHullSpecFile());

        return shipPainter;
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
