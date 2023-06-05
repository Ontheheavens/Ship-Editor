package oth.shipeditor.communication.events.viewer.control;

import oth.shipeditor.communication.events.viewer.ViewerEvent;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record ViewerGuidesToggled(
        boolean guidesEnabled,
        boolean bordersEnabled,
        boolean centerEnabled,
        boolean axesEnabled
    ) implements ViewerEvent {

}
