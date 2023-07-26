package oth.shipeditor.undo.edits;

import oth.shipeditor.components.viewer.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface PointEdit {

    WorldPoint getPoint();

}
