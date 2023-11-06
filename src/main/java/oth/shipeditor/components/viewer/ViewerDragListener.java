package oth.shipeditor.components.viewer;

import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;

/**
 * @author Ontheheavens
 * @since 06.11.2023
 */
public class ViewerDragListener extends DragSourceAdapter {

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        super.dragDropEnd(dsde);
        ViewerDropReceiver.finishDragToViewer();
    }

}
