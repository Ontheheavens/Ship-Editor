package oth.shipeditor.communication;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.utility.Errors;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a very simple implementation of EventBus-type Observer pattern, meant to decouple different parts of the app.
 * <p>
 * Initially implemented in a much more elaborate form with generics and selective dispatch by class metadata.
 * Yet, excessive complexity here only leads to more issues down the road; best to KISS here.
 * @author Ontheheavens
 * @since 09.05.2023
 */
@Log4j2
public final class EventBus {

    private static final EventBus bus = new EventBus();

    private final Set<BusEventListener> subscribers;

    private EventBus() {
        this.subscribers = ConcurrentHashMap.newKeySet();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static BusEventListener subscribe(BusEventListener listener) {
        bus.subscribers.add(listener);
        log.trace("Added listener {}, bus size: {}",
                EventBus.getListenerName(listener), bus.subscribers.size());
        return listener;
    }

    public static void unsubscribe(BusEventListener listener) {
        bus.subscribers.remove(listener);
        log.trace("Removed listener {}, bus size: {}",
                EventBus.getListenerName(listener), bus.subscribers.size());
    }

    public static void publish(BusEvent event) {
        try {
            // Without preventive error handling the debug is GNARLY here. Needs refactor to minimize looping time!
            for (BusEventListener receiver : bus.subscribers) {
                receiver.handleEvent(event);
            }
        } catch (Throwable throwable) {
            Errors.printToStream(throwable);
        }
    }

    private static String getListenerName(BusEventListener listener) {
        Class<? extends BusEventListener> identity = listener.getClass();
        String shortName = identity.getSimpleName();
        String pattern = "/0x.*";
        // Apply the pattern and trim the string.
        return shortName.replaceAll(pattern, "");
    }

}
