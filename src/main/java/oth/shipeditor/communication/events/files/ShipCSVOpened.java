package oth.shipeditor.communication.events.files;

import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
public record ShipCSVOpened(List<Map<String, String>> csvData, String packageName) implements FileEvent {
}
