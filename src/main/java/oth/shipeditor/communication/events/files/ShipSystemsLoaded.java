package oth.shipeditor.communication.events.files;

import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 01.08.2023
 */
public record ShipSystemsLoaded(Map<Path, List<ShipSystemCSVEntry>> systemsByPackage) implements FileEvent {

}
