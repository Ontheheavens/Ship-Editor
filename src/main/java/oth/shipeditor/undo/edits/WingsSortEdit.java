package oth.shipeditor.undo.edits;

import lombok.AllArgsConstructor;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 18.10.2023
 */
@AllArgsConstructor
public class WingsSortEdit extends AbstractEdit implements LayerEdit {

    private final List<WingCSVEntry> oldList;

    private final List<WingCSVEntry> newList;

    private ShipLayer layer;

    private final Consumer<List<WingCSVEntry>> sortSetter;

    @Override
    public void undo() {
        sortSetter.accept(oldList);
        if (StaticController.getActiveLayer() == layer) {
            EventBus.publish(new ActiveLayerUpdated(layer));
        }
    }

    @Override
    public void redo() {
        sortSetter.accept(newList);
        if (StaticController.getActiveLayer() == layer) {
            EventBus.publish(new ActiveLayerUpdated(layer));
        }
    }

    @Override
    public String getName() {
        return "Sort Wings";
    }

    @Override
    public LayerPainter getLayerPainter() {
        return layer.getPainter();
    }

    @Override
    public void cleanupReferences() {
        layer = null;
    }

}
