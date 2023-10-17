package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.HullSize;
import oth.shipeditor.representation.ShipSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 03.08.2023
 */
@Log4j2
@Getter
public class WingCSVEntry implements OrdnancedCSVEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String wingID;

    @Setter
    private String displayedName;

    @Getter
    private ShipSpecFile wingMemberSpec;

    private Sprite memberSprite;

    public WingCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        wingID = this.rowData.get("id");
    }

    @Override
    public String getID() {
        return wingID;
    }

    @Override
    public String toString() {
        String name = rowData.get(StringConstants.ID);
        if (name.isEmpty()) {
            name = StringValues.UNTITLED;
        }
        return name;
    }

    public VariantFile retrieveMemberVariant() {
        String variantID = rowData.get(StringConstants.VARIANT);
        var gameData = SettingsManager.getGameData();
        var allVariants = gameData.getAllVariants();
        return allVariants.get(variantID);
    }

    private ShipSpecFile retrieveSpec() {
        VariantFile variantFile = retrieveMemberVariant();

        String hullID = variantFile.getHullId();
        ShipSpecFile desiredSpec = null;

        var gameData = SettingsManager.getGameData();
        var allShipEntries = gameData.getAllShipEntries();
        outer: for (ShipCSVEntry shipEntry : allShipEntries.values()) {
            String shipEntryHullID = shipEntry.getHullID();
            if (shipEntryHullID.equals(hullID)) {
                desiredSpec = shipEntry.getHullSpecFile();
                break;
            } else {
                var skins = shipEntry.getSkins();
                if (skins == null || skins.isEmpty()) continue;
                for (SkinSpecFile skinSpec : skins.values()) {
                    String skinHullId = skinSpec.getSkinHullId();
                    if (skinHullId != null && skinHullId.equals(hullID)) {
                        desiredSpec = skinSpec;
                        break outer;
                    }
                }
            }
        }
        this.wingMemberSpec = desiredSpec;
        return desiredSpec;
    }

    private Sprite getWingMemberSprite() {
        if (this.memberSprite != null) {
            return this.memberSprite;
        }

        ShipSpecFile specFile;
        if (this.wingMemberSpec == null) {
            specFile = this.retrieveSpec();
        } else {
            specFile = this.wingMemberSpec;
        }

        if (specFile != null) {
            String spriteName = specFile.getSpriteName();
            Path of = Path.of(spriteName);
            File spriteFile = FileLoading.fetchDataFile(of, packageFolderPath);
            Sprite result = FileLoading.loadSprite(spriteFile);
            this.memberSprite = result;
            return result;
        } else {
            JOptionPane.showMessageDialog(null,
                    "Wing member sprite loading failed, exception thrown for: " + this.wingID,
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Could not retrieve wing member sprite!");
        }
    }

    /**
     * @param size irrelevant, should be null.
     */
    @Override
    public int getOrdnanceCost(HullSize size) {
        String tableValue = this.rowData.get("op cost");
        return Integer.parseInt(tableValue);
    }

    @Override
    public String getEntryName() {
        if (this.displayedName != null) {
            return this.displayedName;
        }

        ShipSpecFile specFile;
        if (this.wingMemberSpec == null) {
            specFile = this.retrieveSpec();
        } else {
            specFile = this.wingMemberSpec;
        }

        if (specFile != null) {
            var variant = this.retrieveMemberVariant();
            String result = wingMemberSpec.getHullName();
            String drone = "Drone";
            String displayName = variant.getDisplayName();
            if (!(result.endsWith(drone) && displayName.equals(drone))) {
                result = result + " " + displayName;
            }
            this.setDisplayedName(result);
            return result;
        }

        return this.getWingID();
    }

    @Override
    public JLabel getIconLabel() {
        return getIconLabel(32);
    }

    @Override
    public JLabel getIconLabel(int maxSize) {
        Sprite sprite = this.getWingMemberSprite();
        if (sprite != null) {
            String tooltip = Utility.getTooltipForSprite(sprite);
            return ComponentUtilities.createIconFromImage(sprite.image(), tooltip, maxSize);
        }
        return null;
    }

}
