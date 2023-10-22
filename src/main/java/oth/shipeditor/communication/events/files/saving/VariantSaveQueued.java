package oth.shipeditor.communication.events.files.saving;

import oth.shipeditor.communication.events.files.FileEvent;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public record VariantSaveQueued(ShipVariant variant) implements FileEvent {

}
