package oth.shipeditor.communication.events.viewer.layers;

/**
 * This one does not have a target layer argument; it assumes that target is always an active layer.
 * @author Ontheheavens
 * @since 10.06.2023
 */
public record LayerOpacityChangeQueued(float changedValue) implements LayerEvent {

}
