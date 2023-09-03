package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public class WingAddEdit extends AbstractEdit {

    private final List<WingCSVEntry> wingIndex;

    private final ShipLayer layer;

    private final WingCSVEntry entry;

    public WingAddEdit(List<WingCSVEntry> index, ShipLayer shipLayer, WingCSVEntry hullmod) {
        this.wingIndex = index;
        this.layer = shipLayer;
        this.entry = hullmod;
    }

    @Override
    public void undo() {
        wingIndex.remove(entry);
        if (StaticController.getActiveLayer() == layer) {
            EventBus.publish(new ActiveLayerUpdated(layer));
        }
    }

    @Override
    public void redo() {
        wingIndex.add(entry);
        if (StaticController.getActiveLayer() == layer) {
            EventBus.publish(new ActiveLayerUpdated(layer));
        }
    }

    @Override
    public String getName() {
        return "Add Wing";
    }

}
