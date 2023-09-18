package oth.shipeditor.undo.edits;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.09.2023
 */
@Log4j2
@AllArgsConstructor
public class FeatureUninstallEdit<T extends InstallableEntry> extends AbstractEdit {

    private final Map<String, T> collection;

    private final String slotID;

    private final T feature;

    @Override
    public void undo() {
        collection.put(slotID, feature);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }

    @Override
    public void redo() {
        collection.remove(slotID, feature);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }
    @Override

    public String getName() {
        return "Uninstall Feature";
    }

}
