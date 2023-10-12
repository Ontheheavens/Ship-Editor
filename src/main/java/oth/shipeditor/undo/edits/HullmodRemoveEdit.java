package oth.shipeditor.undo.edits;

import lombok.AllArgsConstructor;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@AllArgsConstructor
public class HullmodRemoveEdit extends AbstractEdit implements LayerEdit {

    private final List<HullmodCSVEntry> hullmodIndex;

    private ShipLayer layer;

    private final HullmodCSVEntry entry;

    private int positionIndex;

    @Override
    public void undo() {
        hullmodIndex.add(positionIndex, entry);
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

    @Override
    public LayerPainter getLayerPainter() {
        return layer.getPainter();
    }

    @Override
    public void cleanupReferences() {
        layer = null;
    }

}
