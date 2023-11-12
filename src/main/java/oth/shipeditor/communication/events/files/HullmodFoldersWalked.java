package oth.shipeditor.communication.events.files;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.07.2023
 */
public record HullmodFoldersWalked(Map<Path, List<HullmodCSVEntry>> hullmodsByPackage) implements FileEvent {

}
