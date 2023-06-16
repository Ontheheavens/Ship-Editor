package oth.shipeditor.undo;

import lombok.extern.log4j.Log4j2;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * @author Ontheheavens
 * @since 15.06.2023
 */
@Log4j2
public final class UndoOverseer extends UndoManager {

    private static final UndoOverseer seer = new UndoOverseer();

    private UndoOverseer() {}

    public static UndoableEdit getLastEdit() {
        return seer.lastEdit();
    }

    public static void post(UndoableEdit edit) {
        seer.addEdit(edit);
    }

    public static void undoEdit() {
        if (seer.canUndo()) {
            seer.undo();
        }
    }

}
