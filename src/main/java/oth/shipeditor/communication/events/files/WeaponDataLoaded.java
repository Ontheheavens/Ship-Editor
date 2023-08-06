package oth.shipeditor.communication.events.files;

import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
public record WeaponDataLoaded(Map<Path, Map<String, WeaponCSVEntry>> weaponsByPackage) implements FileEvent {

}
