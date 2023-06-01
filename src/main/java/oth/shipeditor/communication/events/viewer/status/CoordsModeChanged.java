package oth.shipeditor.communication.events.viewer.status;

import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.components.CoordsDisplayMode;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public record CoordsModeChanged(CoordsDisplayMode newMode) implements BusEvent {

}
