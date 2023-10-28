package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.parsing.FileUtilities;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 03.08.2023
 */
@Log4j2
abstract class LoadCSVDataAction<T extends CSVEntry> extends DataLoadingAction {

    private final Path targetFile;

    LoadCSVDataAction(Path target) {
        this.targetFile = target;
    }

    @Override
    public Runnable perform() {
        log.info("Commencing CSV data fetching...");
        Map<Path, File> tableWithPackage = FileUtilities.getFileFromPackages(targetFile);
        Map<String, List<T>> entriesByPackage = new HashMap<>();
        for (Map.Entry<Path, File> folder : tableWithPackage.entrySet()) {
            log.info("Loading CSV table from package: {}", folder.getKey());
            List<T> systemsList = loadPackage(folder.getKey(),
                    folder.getValue());
            entriesByPackage.putIfAbsent(String.valueOf(folder.getKey()), systemsList);
        }
        return () -> publishResult(entriesByPackage);
    }

    protected abstract void publishResult(Map<String, List<T>> entriesByPackage);

    protected abstract T instantiateEntry(Map<String, String> row, Path folderPath, Path dataFilePath);

    List<Map<String, String>> parseTable(Path dataFilePath) {
        return FileLoading.parseCSVTable(dataFilePath);
    }

    private List<T> loadPackage(Path folderPath, File table) {
        Path dataFilePath = table.toPath();

        List<Map<String, String>> csvData = parseTable(dataFilePath);

        List<T> shipSystemList = new ArrayList<>(csvData.size());
        for (Map<String, String> row : csvData) {
            String rowId = row.get("id");
            if (rowId != null && !rowId.isEmpty()) {
                T newEntry = instantiateEntry(row, folderPath, dataFilePath);
                shipSystemList.add(newEntry);
            }
        }
        return shipSystemList;
    }

}
