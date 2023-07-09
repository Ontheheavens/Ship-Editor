package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.StringConstants;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
@Log4j2
@Getter
public class HullmodCSVEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String hullmodID;

    private final String spriteFileName;

    private File fetchedSpriteFile;

    public HullmodCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        hullmodID = this.rowData.get("id");
        spriteFileName = this.rowData.get("sprite");
    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = "UNTITLED";
        }
        return displayedName;
    }


    public File fetchSpriteFile() {
        if (fetchedSpriteFile != null) return fetchedSpriteFile;
        String path = this.spriteFileName;
        if (path == null || path.isEmpty()) {
            // Fallback sprite with question mark.
            path = "graphics/icons/intel/investigation.png";
        };
        Path spritePath = Path.of(path);
        Path coreDataFolder = SettingsManager.getCoreFolderPath();
        List<Path> otherModFolders = SettingsManager.getAllModFolders();
        File result;

        // Search in parent mod package.
        result = FileUtilities.searchFileInFolder(spritePath, this.packageFolderPath);

        // If not found, search in core folder.
        if (result == null) {
            result = FileUtilities.searchFileInFolder(spritePath, coreDataFolder);
        }
        if (result != null) return result;

        // If not found, search in other mods.
        for (Path modFolder : otherModFolders) {
            result = FileUtilities.searchFileInFolder(spritePath, modFolder);
            if (result != null) {
                break;
            } else {
                log.error("Failed to fetch sprite file for {}!", this.getHullmodID());
            }
        }
        fetchedSpriteFile = result;
        return fetchedSpriteFile;
    }

}
