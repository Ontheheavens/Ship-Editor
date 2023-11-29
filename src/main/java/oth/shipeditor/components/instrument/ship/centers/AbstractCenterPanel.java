package oth.shipeditor.components.instrument.ship.centers;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.components.instrument.EditorInstrument;
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
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode == getMode()) {
                    this.handleRefreshFromLayer(StaticController.getActiveLayer());
                }
            }
        });
    }

    protected abstract EditorInstrument getMode();

}
