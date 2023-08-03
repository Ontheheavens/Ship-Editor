package oth.shipeditor.components.datafiles.entities;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 03.08.2023
 */
public interface CSVEntry {

    Map<String, String> getRowData();

    Path getPackageFolderPath();

}
