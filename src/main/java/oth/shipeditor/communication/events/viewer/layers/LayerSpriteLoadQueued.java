package oth.shipeditor.communication.events.viewer.layers;

import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.utility.graphics.Sprite;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
public record LayerSpriteLoadQueued(ViewerLayer updated, Sprite sprite) implements LayerEvent {

}
