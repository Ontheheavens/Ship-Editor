package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.instrument.EditorInstrument;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record InstrumentModeChanged(EditorInstrument newMode) implements PointEvent {

}
