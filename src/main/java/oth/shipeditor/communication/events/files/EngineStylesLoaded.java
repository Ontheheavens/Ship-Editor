package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.ship.EngineStyle;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 19.08.2023
 */
public record EngineStylesLoaded(Map<String, EngineStyle> engineStyles) implements FileEvent {

}
