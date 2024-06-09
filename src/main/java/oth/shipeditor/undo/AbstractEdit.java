package oth.shipeditor.undo;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author Ontheheavens
 * @since 16.06.2023
 */
@Getter
public abstract class AbstractEdit implements Edit {

    /**
     * These are meant to be consequential after the parent edit, meaning that first undo() of sub-edits is invoked.
     * Sub-edits are undone from head to tail, after that the parent layer is undone.
     */
    private final Deque<Edit> subEdits = new ArrayDeque<>();

    @Setter
    private boolean finished = true;

    @Override
    public void add(Edit edit) {
        subEdits.addFirst(edit);
    }

    protected void undoSubEdits() {
        subEdits.forEach(Edit::undo);
    }

    protected void redoSubEdits() {
        List<Edit> editsList = new ArrayList<>(subEdits);
        Collections.reverse(editsList);
        editsList.forEach(Edit::redo);
    }

    @Override
    public String toString() {
        Class<? extends AbstractEdit> identity = this.getClass();
        return identity.getSimpleName() + " " + hashCode();
    }

}
