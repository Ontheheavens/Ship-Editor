package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
public record HullFolderWalked(List<Map<String, String>> csvData, Map<String, HullSpecFile> hullFiles,
                               Map<String, SkinSpecFile> skinFiles, Path folder) implements FileEvent {
}
