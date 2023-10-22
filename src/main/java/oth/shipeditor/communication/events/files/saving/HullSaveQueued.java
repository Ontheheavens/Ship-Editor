package oth.shipeditor.communication.events.files.saving;

import oth.shipeditor.communication.events.files.FileEvent;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public record HullSaveQueued(ShipLayer shipLayer) implements FileEvent {

}
