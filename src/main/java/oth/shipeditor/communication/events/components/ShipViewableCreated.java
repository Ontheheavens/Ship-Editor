package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.ShipViewable;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public record ShipViewableCreated(ShipViewable viewable) implements ComponentEvent {

}
