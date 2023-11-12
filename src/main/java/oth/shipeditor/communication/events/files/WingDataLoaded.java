package oth.shipeditor.communication.events.files;

import oth.shipeditor.components.datafiles.entities.WingCSVEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 03.08.2023
 */
public record WingDataLoaded(Map<Path, List<WingCSVEntry>> wingsByPackage) implements FileEvent {

}
