package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.AbstractEdit;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public class AnchorOffsetEdit extends AbstractEdit {

    private final LayerPainter shipPainter;

    private final Point2D oldOffset;

    private final Point2D updatedOffset;

    public AnchorOffsetEdit(LayerPainter painter, Point2D old, Point2D updated) {
        this.shipPainter = painter;
        this.oldOffset = old;
        this.updatedOffset = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        shipPainter.setAnchor(oldOffset);
        Events.repaintView();
    }

    @Override
    public void redo() {
        shipPainter.setAnchor(updatedOffset);
        redoSubEdits();
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Anchor Offset";
    }

}
