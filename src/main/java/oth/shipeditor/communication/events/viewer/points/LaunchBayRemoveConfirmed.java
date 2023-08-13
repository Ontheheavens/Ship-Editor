package oth.shipeditor.communication.events.viewer.points;

import oth.shipeditor.components.viewer.entities.bays.LaunchBay;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public record LaunchBayRemoveConfirmed(LaunchBay removed) implements PointEvent {

}
