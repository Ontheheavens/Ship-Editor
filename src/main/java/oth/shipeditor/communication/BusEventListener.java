package oth.shipeditor.communication;

import oth.shipeditor.communication.events.BusEvent;

/**
 * @author Ontheheavens
 * @since 10.05.2023
 */
@FunctionalInterface
public interface BusEventListener {

    void handleEvent(BusEvent event);

}
