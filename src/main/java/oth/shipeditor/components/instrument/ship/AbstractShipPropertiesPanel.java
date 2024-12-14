package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;

/**
 * @author Ontheheavens
 * @since 11.11.2023
 */
public abstract class AbstractShipPropertiesPanel extends LayerPropertiesPanel {

    protected AbstractShipPropertiesPanel() {
        this.initLayerListeners();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    protected void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                var selected = checked.selected();
                this.handleRefreshFromLayer(selected);
            } else if (event instanceof ActiveLayerUpdated checked) {
                this.handleRefreshFromLayer(checked.updated());
            }
        });
    }

    protected void handleRefreshFromLayer(ViewerLayer selected) {
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
