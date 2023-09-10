package oth.shipeditor.undo.edits.points;

import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.undo.edits.LayerEdit;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public interface PointEdit extends LayerEdit {

    WorldPoint getPoint();

}
