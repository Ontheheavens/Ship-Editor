package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public class HullmodRemoveEdit extends AbstractEdit {

    private final List<HullmodCSVEntry> hullmodIndex;

    private final ShipLayer layer;

    private final HullmodCSVEntry entry;

    public HullmodRemoveEdit(List<HullmodCSVEntry> index, ShipLayer shipLayer, HullmodCSVEntry hullmod) {
        this.hullmodIndex = index;
        this.layer = shipLayer;
        this.entry = hullmod;
    }

    @Override
    public void undo() {
        hullmodIndex.add(entry);
        if (StaticController.getActiveLayer() == layer) {
            EventBus.publish(new ActiveLayerUpdated(layer));
        }
    }

    @Override
    public void redo() {
        hullmodIndex.remove(entry);
        if (StaticController.getActiveLayer() == layer) {
            EventBus.publish(new ActiveLayerUpdated(layer));
        }
    }

    @Override
    public String getName() {
        return "Remove Hullmod";
    }

}
