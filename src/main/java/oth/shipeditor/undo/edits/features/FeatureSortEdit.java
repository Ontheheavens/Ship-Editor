package oth.shipeditor.undo.edits.features;

import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 19.09.2023
 */
public class FeatureSortEdit<T extends InstallableEntry> extends AbstractEdit {

    private final Consumer<Map<String, T>> setter;

    private final Map<String, T> oldCollection;

    private final Map<String, T> newCollection;

    public FeatureSortEdit(Consumer<Map<String, T>> consumer, Map<String, T>  oldMap,
                           Map<String, T> newMap) {
        this.setter = consumer;
        this.oldCollection = oldMap;
        this.newCollection = newMap;
    }

    @Override
    public void undo() {
        setter.accept(oldCollection);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }

    @Override
    public void redo() {
        setter.accept(newCollection);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBuiltInsRepaint();
    }

    @Override
    public String getName() {
        return "Sort Features";
    }

}
