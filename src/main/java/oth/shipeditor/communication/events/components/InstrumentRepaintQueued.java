package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.instrument.EditorInstrument;

/**
 * @author Ontheheavens
 * @since 27.11.2023
 */
public record InstrumentRepaintQueued(EditorInstrument editorMode) implements ComponentEvent {

}
