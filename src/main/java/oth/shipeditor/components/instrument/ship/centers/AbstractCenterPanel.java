package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 10.11.2023
 */
public abstract class AbstractCenterPanel extends AbstractShipPropertiesPanel {

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof CenterPanelsRepaintQueued) {
                this.handleRefreshFromLayer(StaticController.getActiveLayer());
            }
        });
    }

}
