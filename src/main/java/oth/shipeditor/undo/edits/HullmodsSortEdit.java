package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 16.10.2023
 */
public class HullmodsSortEdit extends AbstractEdit implements LayerEdit  {

    private final List<HullmodCSVEntry> oldList;

    private final List<HullmodCSVEntry> newList;

    private ShipLayer layer;

    private final Consumer<List<HullmodCSVEntry>> sortSetter;

    public HullmodsSortEdit(List<HullmodCSVEntry> old, List<HullmodCSVEntry> updated, ShipLayer shipLayer,
                            Consumer<List<HullmodCSVEntry>> setter) {
        this.oldList = old;
        this.newList = updated;
        this.layer = shipLayer;
        this.sortSetter = setter;
    }

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
        return "Sort Hullmods";
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
