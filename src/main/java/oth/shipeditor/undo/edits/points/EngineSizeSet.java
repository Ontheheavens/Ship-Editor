package oth.shipeditor.undo.edits.points;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.Size2D;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineSizeSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final Size2D oldSize;

    private final Size2D updatedSize;

    public EngineSizeSet(EnginePoint point, Size2D old, Size2D updated) {
        this.enginePoint = point;
        this.oldSize = old;
        this.updatedSize = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        enginePoint.setSize(oldSize);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public void redo() {
        enginePoint.setSize(updatedSize);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Engine Size";
    }

}
