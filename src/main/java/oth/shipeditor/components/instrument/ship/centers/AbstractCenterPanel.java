package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.components.containers.LayerPropertiesPanel;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 10.11.2023
 */
public abstract class AbstractCenterPanel extends LayerPropertiesPanel {

    AbstractCenterPanel() {
        this.initLayerListeners();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    protected void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected(var old, var selected)) {
                this.handleRefreshFromLayer(selected);
            } else if (event instanceof ActiveLayerUpdated(var updated)) {
                this.handleRefreshFromLayer(updated);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof CenterPanelsRepaintQueued) {
                this.handleRefreshFromLayer(StaticController.getActiveLayer());
            }
        });
    }

    private void handleRefreshFromLayer(ViewerLayer selected) {
        boolean layerPainterPresent = selected != null && selected.getPainter() != null;
        if (!layerPainterPresent) {
            this.refresh(null);
            return;
        }
        LayerPainter layerPainter = selected.getPainter();
        if (!(layerPainter instanceof ShipPainter shipPainter) || layerPainter.isUninitialized()) {
            this.refresh(null);
            return;
        }

        this.refresh(selected.getPainter());
    }

}
