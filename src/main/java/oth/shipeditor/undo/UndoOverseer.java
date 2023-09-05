package oth.shipeditor.undo;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.edits.LayerEdit;
import oth.shipeditor.undo.edits.ListeningEdit;
import oth.shipeditor.undo.edits.points.PointDragEdit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;

/**
 * @author Ontheheavens
 * @since 15.06.2023
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
@Log4j2
public final class UndoOverseer {

    private static final UndoOverseer seer = new UndoOverseer();

    private UndoOverseer() {
        undoAction.setEnabled(false);
        redoAction.setEnabled(false);
    }

    /**
     * Isn't meant to have protective checks; the input vehicles need to be disabled if there is no edits in stack.
     */
    private final Action undoAction = new AbstractAction("Undo") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Deque<Edit> undo = seer.getUndoStack();
            Edit head = undo.pop();
            head.undo();
            Deque<Edit> redo = seer.getRedoStack();
            redo.push(head);
            updateActionState();
        }
    };

    private final Action redoAction = new AbstractAction("Redo") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Deque<Edit> redo = getRedoStack();
            Edit head = redo.pop();
            head.redo();
            Deque<Edit> undo = getUndoStack();
            undo.push(head);
            updateActionState();
        }
    };

    @Getter
    private final Deque<Edit> undoStack = new ArrayDeque<>();

    @Getter
    private final Deque<Edit> redoStack = new ArrayDeque<>();

    private void updateActionState() {
        Deque<Edit> undo = seer.getUndoStack();
        String undoName = "Undo";
        if (undo.isEmpty()) {
            undoAction.setEnabled(false);
        } else {
            undoAction.setEnabled(true);
            Edit nextUndoable = UndoOverseer.getNextUndoable();
            undoName = undoName + " " + nextUndoable.getName();
        }
        undoAction.putValue(Action.NAME, undoName);
        undoAction.putValue(Action.SHORT_DESCRIPTION, undoName);

        Deque<Edit> redo = seer.getRedoStack();
        String redoName = "Redo";
        if (redo.isEmpty()) {
            redoAction.setEnabled(false);
        } else {
            redoAction.setEnabled(true);
            Edit nextUndoable = UndoOverseer.getNextRedoable();
            redoName = redoName + " " + nextUndoable.getName();
        }
        redoAction.putValue(Action.NAME, redoName);
        redoAction.putValue(Action.SHORT_DESCRIPTION, redoName);
    }

    public static Action getUndoAction() {
        return seer.undoAction;
    }

    public static Action getRedoAction() {
        return seer.redoAction;
    }

    static Edit getNextUndoable() {
        Deque<Edit> stack = seer.getUndoStack();
        return stack.peek();
    }

    private static Edit getNextRedoable() {
        Deque<Edit> stack = seer.getRedoStack();
        return stack.peek();
    }

    static void post(Edit edit) {
        Deque<Edit> stack = seer.getUndoStack();
        stack.addFirst(edit);
        UndoOverseer.clearRedoStack();
        seer.updateActionState();
    }

    private static void clearRedoStack() {
        UndoOverseer.clearEditListeners(seer.redoStack);
        seer.redoStack.clear();
    }

    private static void clearEditListeners(Iterable<Edit> stack) {
        stack.forEach(edit -> {
            if (edit instanceof ListeningEdit checked) {
                checked.unregisterListeners();
            }
        });
    }

    private static Collection<Edit> getAllEdits() {
        Collection<Edit> allEdits = new ArrayList<>(seer.undoStack);
        allEdits.addAll(seer.redoStack);
        return allEdits;
    }

    public static void adjustPointEditsOffset(BaseWorldPoint point, Point2D offset) {
        var allEdits = UndoOverseer.getAllEdits();
        allEdits.forEach(edit -> {
            if (edit instanceof PointDragEdit dragEdit && dragEdit.getPoint() == point)  {
                dragEdit.adjustPositionOffset(offset);
            }
        });
    }

    public static void finishAllEdits() {
        var allEdits = UndoOverseer.getAllEdits();
        allEdits.forEach(edit -> edit.setFinished(true));
    }

    public static void cleanupRemovedLayer(LayerPainter painter) {
        UndoOverseer.cleanupStack(painter,  seer.undoStack);
        UndoOverseer.cleanupStack(painter,  seer.redoStack);
        seer.updateActionState();
    }

    private static void cleanupStack(LayerPainter painter, Collection<Edit> stack) {
        Collection<Edit> toRemove = new ArrayList<>();
        for (Edit edit : stack) {
            if (edit instanceof LayerEdit checked) {
                LayerPainter layerPainter = checked.getLayerPainter();
                if (layerPainter == null || layerPainter != painter) continue;
                checked.cleanupReferences();
                toRemove.add(edit);
                if (checked instanceof ListeningEdit listeningEdit) {
                    listeningEdit.unregisterListeners();
                }
            }
        }
        stack.removeAll(toRemove);
    }

}
