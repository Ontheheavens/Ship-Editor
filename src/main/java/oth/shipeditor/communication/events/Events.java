package oth.shipeditor.communication.events;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundsPanelRepaintQueued;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.components.SlotsPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;

/**
 * Convenience class for routine event bus declarations.
 * @author Ontheheavens
 * @since 15.06.2023
 */
public final class Events {

    private Events() {
    }

    public static void repaintView() {
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new BoundsPanelRepaintQueued());
        EventBus.publish(new CenterPanelsRepaintQueued());
        EventBus.publish(new SlotsPanelRepaintQueued());
    }

}
