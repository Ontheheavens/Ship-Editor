package oth.shipeditor.components;

import oth.shipeditor.components.entities.BaseWorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public interface PointsDisplay<T extends BaseWorldPoint> {

    enum InteractionMode {
        DISABLED, SELECT, CREATE
    }

}
