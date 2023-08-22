package oth.shipeditor.undo.edits.points.engines;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 22.08.2023
 */
public class EngineStyleSet extends AbstractEdit {

    private final EnginePoint enginePoint;

    private final EngineStyle oldStyle;

    private final EngineStyle updatedStyle;

    public EngineStyleSet(EnginePoint point, EngineStyle old, EngineStyle updated) {
        this.enginePoint = point;
        this.oldStyle = old;
        this.updatedStyle = updated;
    }

    @Override
    public void undo() {
        enginePoint.setStyle(oldStyle);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public void redo() {
        enginePoint.setStyle(updatedStyle);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new EnginesPanelRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Engine Style";
    }

}
