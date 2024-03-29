package oth.shipeditor.communication.events;

import oth.shipeditor.utility.overseers.StaticController;

/**
 * Convenience class for routine event bus declarations.
 * @author Ontheheavens
 * @since 15.06.2023
 */
public final class Events {

    private Events() {
    }

    public static void repaintShipView() {
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueBoundsPanelRepaint();
        repainter.queueCenterPanelsRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueVariantsRepaint();
    }

}
