package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.instrument.AbstractInstrumentsPane;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
public record InstrumentSplitterResized(AbstractInstrumentsPane source, boolean minimized) implements ComponentEvent {

}
