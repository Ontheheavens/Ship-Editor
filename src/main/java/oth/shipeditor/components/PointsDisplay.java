package oth.shipeditor.components;

import oth.shipeditor.components.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public interface PointsDisplay<T extends WorldPoint> {

    enum InteractionMode {
        DISABLED, SELECT, CREATE
    }

}
