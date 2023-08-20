package oth.shipeditor.undo.edits.points;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EngineAngleSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final double oldAngle;

    private final double updatedAngle;

    public EngineAngleSet(EnginePoint point, double old, double updated) {
        this.enginePoint = point;
        this.oldAngle = old;
        this.updatedAngle = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        enginePoint.setAngle(oldAngle);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public void redo() {
        enginePoint.setAngle(updatedAngle);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Engine Angle";
    }

}
