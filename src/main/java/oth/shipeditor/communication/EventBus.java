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
public final class EventBus {

    private static final EventBus bus = new EventBus();

    private final Set<BusEventListener> subscribers;

    private EventBus() {
        this.subscribers = new HashSet<>();
    }

    public static BusEventListener subscribe(BusEventListener listener) {
        bus.subscribers.add(listener);
        log.info("Added listener, bus size: {}", bus.subscribers.size());
        return listener;
    }

    // TODO: Right now listener are not getting removed from bus at all.
    //  Since the number of listeners is comparatively small so far, this is not a problem yet.
    //  If situation arises where the event bus bloat proves an issue, we will need to refactor our whole listener system.

    /**
     * Unused right now but might prove useful later.
     * @param listener instance that needs to be removed from Event Bus pool of event receivers.
     */
    @SuppressWarnings("unused")
    public static void unsubscribe(BusEventListener listener) {
        bus.subscribers.remove(listener);
    }

    public static void publish(BusEvent event) {
        Iterable<BusEventListener> receivers = new HashSet<>(bus.subscribers);
        for (BusEventListener receiver : receivers) {
            receiver.handleEvent(event);
        }
    }

}
