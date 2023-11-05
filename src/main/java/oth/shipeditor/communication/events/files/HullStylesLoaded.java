package oth.shipeditor.communication.events.files;

import oth.shipeditor.representation.ship.HullStyle;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 23.07.2023
 */
public record HullStylesLoaded(Map<String, HullStyle> hullStyles) implements FileEvent {

}
