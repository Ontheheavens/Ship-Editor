package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 27.09.2023
 */
public abstract class AbstractVariantPanel extends JPanel {

    AbstractVariantPanel() {
        initLayerListeners();
    }

    public abstract void refreshPanel(ViewerLayer selected);

    @SuppressWarnings("ChainOfInstanceofChecks")
    protected void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                this.refreshPanel(checked.selected());
            } else if (event instanceof ActiveLayerUpdated checked) {
                this.refreshPanel(checked.updated());
            }
        });
    }

}
