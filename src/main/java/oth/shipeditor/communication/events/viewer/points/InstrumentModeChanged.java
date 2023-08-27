package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.instrument.ship.ShipInstrument;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record InstrumentModeChanged(ShipInstrument newMode) implements PointEvent {

}
