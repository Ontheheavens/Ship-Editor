package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.InstrumentMode;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record InstrumentModeChanged(InstrumentMode newMode) implements PointEvent {

}
