package oth.shipeditor.undo.edits.features;

import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 05.10.2023
 */
public class ModulesSortEdit extends AbstractEdit {

    private final Consumer<Map<String, InstalledFeature>> setter;

    private final Map<String, InstalledFeature> oldCollection;

    private final Map<String, InstalledFeature> newCollection;

    public ModulesSortEdit(Consumer<Map<String, InstalledFeature>> consumer,
                           Map<String, InstalledFeature>  oldMap,
                           Map<String, InstalledFeature> newMap) {
        this.setter = consumer;
        this.oldCollection = oldMap;
        this.newCollection = newMap;
    }

    @Override
    public void undo() {
        setter.accept(oldCollection);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueModulesRepaint();
    }

    @Override
    public void redo() {
        setter.accept(newCollection);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueModulesRepaint();
    }

    @Override
    public String getName() {
        return "Sort Modules";
    }

}
