package oth.shipeditor.components.viewer;

import oth.shipeditor.components.viewer.entities.BaseWorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public interface PointsDisplay<T extends BaseWorldPoint> {

    enum InteractionMode {
        DISABLED, SELECT, CREATE
    }

}
