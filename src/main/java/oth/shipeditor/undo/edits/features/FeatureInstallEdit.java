package oth.shipeditor.undo.edits.features;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 21.09.2023
 */
@Log4j2
@AllArgsConstructor
public class FeatureInstallEdit<T extends InstallableEntry> extends AbstractEdit {

    private final Map<String, T> collection;

    private final String slotID;

    private final T feature;

    /**
     * Can be null, needed for skin built-ins reloading.
     */
    private final Runnable afterAction;

    @Override
    public void undo() {
        collection.remove(slotID, feature);
        if (afterAction != null) {
            afterAction.run();
        }
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueVariantsRepaint();
    }

    @Override
    public void redo() {
        collection.put(slotID, feature);
        if (afterAction != null) {
            afterAction.run();
        }
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueVariantsRepaint();
    }
    @Override

    public String getName() {
        return "Install Feature";
    }

}
