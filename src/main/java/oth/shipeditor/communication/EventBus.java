package oth.shipeditor.communication;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.events.BusEvent;

import java.util.HashSet;
import java.util.Set;

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
        this.subscribers = new HashSet<>();
    }

    public static BusEventListener subscribe(BusEventListener listener) {
        bus.subscribers.add(listener);
        log.info("Added listener {}, bus size: {}",
                EventBus.getListenerName(listener), bus.subscribers.size());
        return listener;
    }

    // TODO: Right now listener are not getting removed from bus at all.
    //  Since the number of listeners is comparatively small so far, this is not a problem yet.
    //  If situation arises where the event bus bloat proves an issue, we will need to refactor our whole listener system.
    //  One of the solutions for that is to swap lambdas for anonymous classes and have unsubscribe event condition.

    // Note: not all event listeners are equally perpetrators in this issue; some are registered and persist
    // through whole runtime duration, while others are registered en masse in span of seconds. We should deal with the latter,
    // and leave the former be.
    public static void unsubscribe(BusEventListener listener) {
        bus.subscribers.remove(listener);
        log.info("Removed listener {}, bus size: {}",
                EventBus.getListenerName(listener), bus.subscribers.size());
    }

    public static void publish(BusEvent event) {
        Iterable<BusEventListener> receivers = new HashSet<>(bus.subscribers);
        for (BusEventListener receiver : receivers) {
            receiver.handleEvent(event);
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
