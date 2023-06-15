package oth.shipeditor.undo;

import lombok.extern.log4j.Log4j2;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * @author Ontheheavens
 * @since 15.06.2023
 */
@Log4j2
public abstract class ViewerEdit extends AbstractUndoableEdit {

    public abstract void undoImpl();

    public abstract void redoImpl();

    @Override
    public void undo() {
        log.info("TTTTTTTTTTTTTTTTTTT");
        super.undo();
        this.undoImpl();
    }

    @Override
    public void redo() {
        super.redo();
        this.redoImpl();
    }

}
