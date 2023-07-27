package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public class LayerRotationEdit extends AbstractEdit {

    private final LayerPainter layerPainter;

    private final double oldRotation;

    private final double updatedRotation;

    public LayerRotationEdit(LayerPainter painter, double old, double updated) {
        this.layerPainter = painter;
        this.oldRotation = old;
        this.updatedRotation = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        layerPainter.setRotationRadians(oldRotation);
        Events.repaintView();
    }

    @Override
    public void redo() {
        layerPainter.setRotationRadians(updatedRotation);
        redoSubEdits();
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Rotate Layer";
    }

}
