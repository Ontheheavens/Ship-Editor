package oth.shipeditor.undo.edits.points.engines;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 22.08.2023
 */
public class EngineContrailSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final int oldContrail;

    private final int updatedContrail;

    public EngineContrailSet(EnginePoint point, int old, int updated) {
        this.enginePoint = point;
        this.oldContrail = old;
        this.updatedContrail = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        enginePoint.setContrailSize(oldContrail);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public void redo() {
        enginePoint.setContrailSize(updatedContrail);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Engine Contrail Size";
    }

}
