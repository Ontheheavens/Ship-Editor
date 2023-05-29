package oth.shipeditor.communication.events;

import oth.shipeditor.components.entities.WorldPoint;

/**
 * @author Ontheheavens
 * @since 29.05.2023
 */
public record PointPanelRepaintQueued<T extends WorldPoint>() implements BusEvent {

}
