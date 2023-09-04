package oth.shipeditor.communication.events;

import oth.shipeditor.utility.StaticController;

/**
 * Convenience class for routine event bus declarations.
 * @author Ontheheavens
 * @since 15.06.2023
 */
public final class Events {

    private Events() {
    }

    public static void repaintView() {
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueBoundsPanelRepaint();
        repainter.queueCenterPanelsRepaint();
    }

}
