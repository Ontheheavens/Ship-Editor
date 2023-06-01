package oth.shipeditor.communication;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.events.BusEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
@Log4j2
public class EventBus {

    private static final EventBus bus = new EventBus();

    private final Set<BusEventListener> subscribers;

    private EventBus() {
        this.subscribers = new HashSet<>();
    }

    public static EventBus get() {
        return bus;
    }

    public static BusEventListener subscribe(BusEventListener listener) {
        bus.subscribers.add(listener);
        return listener;
    }

    public static void unsubscribe(BusEventListener listener) {
        bus.subscribers.remove(listener);
    }

    public static void publish(BusEvent event) {
        Set<BusEventListener> receivers = new HashSet<>(bus.subscribers);
        for (BusEventListener receiver : receivers) {
            receiver.handleEvent(event);
        }
    }

}
