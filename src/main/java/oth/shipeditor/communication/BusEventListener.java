package oth.shipeditor.communication;

import oth.shipeditor.communication.events.BusEvent;

/**
 * @author Ontheheavens
 * @since 10.05.2023
 */
public interface BusEventListener<T extends BusEvent> {
    void handleEvent(T event);
}
