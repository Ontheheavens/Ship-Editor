package oth.shipeditor.undo.edits.features;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.09.2023
 */
@Log4j2
@AllArgsConstructor
public class FeatureUninstallEdit<T extends InstallableEntry> extends AbstractEdit {

    private final Map<String, T> collectionBefore;

    private final Map<String, T> collectionAfter;

    private final Map<String, T> collection;

    /**
     * Can be null, needed for skin built-ins reloading.
     */
    private final Runnable invalidator;

    @Override
    public void undo() {
        collection.clear();
        collection.putAll(collectionBefore);
        if (invalidator != null) {
            invalidator.run();
        }
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }

    @Override
    public void redo() {
        collection.clear();
        collection.putAll(collectionAfter);
        if (invalidator != null) {
            invalidator.run();
        }
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }
    @Override

    public String getName() {
        return "Uninstall Feature";
    }

}
