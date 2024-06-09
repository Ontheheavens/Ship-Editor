package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.graphics.Sprite;

/**
 * @author Ontheheavens
 * @since 09.06.2024
 */
public class SpriteSwapEdit extends AbstractEdit implements LayerEdit {

    private LayerPainter layer;

    private final Sprite oldSprite;

    private final Sprite newSprite;

    public SpriteSwapEdit(LayerPainter layer, Sprite oldSprite, Sprite newSprite) {
        this.layer = layer;
        this.oldSprite = oldSprite;
        this.newSprite = newSprite;
    }

    @Override
    public void undo() {
        layer.reconfigureSpriteCircumstance(oldSprite);
        if (layer instanceof ShipPainter shipPainter) {
            shipPainter.setBaseHullSprite(oldSprite);
        }
        EventBus.publish(new ActiveLayerUpdated(layer.getParentLayer()));
    }

    @Override
    public void redo() {
        layer.reconfigureSpriteCircumstance(newSprite);
        if (layer instanceof ShipPainter shipPainter) {
            shipPainter.setBaseHullSprite(newSprite);
        }
        EventBus.publish(new ActiveLayerUpdated(layer.getParentLayer()));
    }

    @Override
    public String getName() {
        return "Sprite Swap";
    }

    @Override
    public LayerPainter getLayerPainter() {
        return layer;
    }

    @Override
    public void cleanupReferences() {
        this.layer = null;
    }

}
