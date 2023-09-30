package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 27.09.2023
 */
@Getter
@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
public abstract class RefreshablePanel extends JPanel {

    protected RefreshablePanel() {
        this.initLayerListeners();
    }

    public abstract void refreshPanel(ViewerLayer selected);

    @SuppressWarnings("ChainOfInstanceofChecks")
    protected void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected(var old, var selected)) {
                this.refreshPanel(selected);
            } else if (event instanceof ActiveLayerUpdated(var updated)) {
                this.refreshPanel(updated);
            }
        });
    }

}
